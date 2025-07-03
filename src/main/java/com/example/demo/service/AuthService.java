package com.example.demo.service;

import com.example.demo.controller.dto.LoginRequestDto;
import com.example.demo.controller.dto.LoginResponseDto;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.repository.UserJpaRepository;
import com.example.demo.usecase.AuthUseCase;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthUseCase {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto, String accessHeader, String refreshTokenFromCookie) {

        UserEntity user = userJpaRepository.findByEmail(loginRequestDto.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 없음"));

        if (!passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호 오류");
        }

        if (accessHeader != null && jwtUtil.isTokenValid(accessHeader)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 로그인된 사용자입니다.");
        }


        if (refreshTokenFromCookie != null ) {
            Long userId = jwtUtil.getUserIdFromToken(refreshTokenFromCookie);

            if (refreshTokenService.refreshTokenValid(userId, refreshTokenFromCookie)) {
                var accessToken = jwtUtil.generateAccessToken(user);
                return new LoginResponseDto(accessToken, refreshTokenFromCookie);
            }
        }

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        log.info("accessToken = {}", accessToken);
        log.info("refreshToken = {}", refreshToken);
        // Redis 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        refreshTokenService.deleteRefreshToken(userId);
    }

    @Override
    public LoginResponseDto reissueAccessToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        if (!refreshTokenService.refreshTokenValid(userId, refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다.");
        }

        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 없음"));

        String newAccessToken = jwtUtil.generateAccessToken(user);

         String newRefreshToken = jwtUtil.generateRefreshToken(user);
         refreshTokenService.saveRefreshToken(userId, newRefreshToken);

        return new LoginResponseDto(newAccessToken, refreshToken);
    }
}
