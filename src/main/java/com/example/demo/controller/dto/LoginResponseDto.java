package com.example.demo.controller.dto;

public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {
}
