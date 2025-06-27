package com.example.demo.util;

import com.example.demo.domain.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long ACCESS_TOKEN_EXP_MS = 1000L * 60 * 15; // 15분
    private static final long REFRESH_TOKEN_EXP_MS = 1000L * 60 * 60 * 24; // 1시간

    // Access Token 생성
    public String generateAccessToken(UserEntity user) {
        return generateToken(user, ACCESS_TOKEN_EXP_MS);
    }

    // Refresh Token 생성
    public String generateRefreshToken(UserEntity user) {
        return generateToken(user, REFRESH_TOKEN_EXP_MS);
    }

    // 공통 토큰 생성 로직
    private String generateToken(UserEntity user, long expirationMs) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId())) // subject = userId
                .claim("email", user.getEmail())
                .claim("nickname", user.getNickname())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 내부 전용 파서 생성기
    private JwtParser parser() {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();
    }

    public boolean isTokenValid(String token) {
        try {
            // Bearer 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }

            parser().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("❌ JWT 검증 실패: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❗ JWT 처리 중 알 수 없는 오류", e);
        }
        return false;
    }


    // Claims 추출
    public Claims parseClaims(String token) {
        return parser().parseClaimsJws(token).getBody();
    }

    // userId 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }
}
