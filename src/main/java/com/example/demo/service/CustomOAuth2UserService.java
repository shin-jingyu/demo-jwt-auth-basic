package com.example.demo.service;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.repository.UserJpaRepository;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserJpaRepository userJpaRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final HttpServletResponse response;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = getOAuth2UserAttributes(registrationId, userRequest);

        String email = (String) attributes.get("email");
        String nickname = (String) attributes.get("nickname");
        String providerId = (String) attributes.get("providerId");

        // 1. 회원가입/로그인 처리
        UserEntity user = userJpaRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(email, nickname, registrationId, providerId));

        // 2. JWT/Refresh Token 발급
        String refreshToken = generateRefreshToken(user);
        String accessToken = jwtUtil.generateAccessToken(user);

        // 3. 응답 처리
        setResponseTokens(refreshToken, accessToken);

        // 4. Spring Security 인증 객체 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );
    }

    private Map<String, Object> getOAuth2UserAttributes(String registrationId, OAuth2UserRequest userRequest) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);  // OAuth2User 객체를 로드

        Map<String, Object> attributes = oAuth2User.getAttributes();  // 사용자 정보

        switch (registrationId) {
            case "google":
                return Map.of(
                        "email", attributes.get("email"),
                        "nickname", attributes.get("name"),
                        "providerId", attributes.get("sub")
                );
            case "naver":
                Map<String, Object> responseAttr = (Map<String, Object>) attributes.get("response");
                return Map.of(
                        "email", responseAttr.get("email"),
                        "nickname", responseAttr.get("name"),
                        "providerId", responseAttr.get("id")
                );
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                // 이메일이 없는 경우, 랜덤 이메일 생성
                String email = (String) kakaoAccount.get("email");
                if (email == null || email.isEmpty()) {
                    // 랜덤 이메일 생성 (예: random-UUID@domain.com)
                    email = "random-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";  // 랜덤 이메일 생성
                }

                return Map.of(
                        "email", email,
                        "nickname", profile.get("nickname"),
                        "providerId", String.valueOf(attributes.get("id"))
                );
            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    }

    // 새로운 사용자 등록 처리
    private UserEntity registerNewUser(String email, String nickname, String registrationId, String providerId) {
        UserEntity newUser = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .provider(registrationId)
                .providerId(providerId)
                .password("OAUTH2_USER") // 임의 값
                .build();
        return userJpaRepository.save(newUser);
    }

    // Refresh Token 생성 및 저장
    private String generateRefreshToken(UserEntity user) {
        String existingRefreshToken = refreshTokenService.getRefreshToken(user.getId());
        String refreshToken;
        if (existingRefreshToken != null && jwtUtil.isTokenValid(existingRefreshToken)) {
            refreshToken = existingRefreshToken; // 기존 토큰이 유효하면 재사용
        } else {
            refreshToken = jwtUtil.generateRefreshToken(user);  // 새로 발급
            refreshTokenService.saveRefreshToken(user.getId(), refreshToken);  // 새 토큰 저장
        }
        return refreshToken;
    }

    // 응답에 엑세스 토큰과 리프레시 토큰 설정
    private void setResponseTokens(String refreshToken, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain;charset=UTF-8");
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.getWriter().write(accessToken);
            response.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("OAuth2 로그인 응답 처리 중 오류 발생", e);
        }
    }
}