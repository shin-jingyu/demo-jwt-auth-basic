package com.example.demo.controller;

import com.example.demo.controller.dto.LoginRequestDto;
import com.example.demo.controller.dto.LoginResponseDto;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.repository.UserJpaRepository;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class JwtController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final UserJpaRepository userJpaRepository;

    @PostMapping("/login")
    public LoginResponseDto login(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LoginRequestDto request)
    {

        if (hasValidAccessToken(authHeader)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 로그인된 사용자입니다.");
        }

        // 유저 조회
        var user = userService.readPetFairSummary(request.email());

        // 비밀번호 검증
        if (!PasswordUtil.matches(request.password(), user.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다.");
        }

        // JWT 발급
        String accessToken = jwtUtil.generateAccessToken(user);   // 이건 아직 안 만들었으면 생략
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Redis 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }

    private boolean hasValidAccessToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);

        return jwtUtil.isTokenValid(token);
    }

}
