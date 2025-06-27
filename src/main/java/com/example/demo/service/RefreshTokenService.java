package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    // 저장
    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = "refresh:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofHours(1));
    }

    // 조회
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get("refresh:" + userId);
    }

    // 삭제
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    // 비교
    public boolean refreshTokenValid(Long userId, String tokenFromClient) {
        String tokenInRedis = getRefreshToken(userId);
        return tokenFromClient.equals(tokenInRedis);
    }
}
