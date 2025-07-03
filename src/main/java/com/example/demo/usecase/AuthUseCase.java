package com.example.demo.usecase;

import com.example.demo.controller.dto.LoginRequestDto;
import com.example.demo.controller.dto.LoginResponseDto;

public interface AuthUseCase {
    LoginResponseDto login(LoginRequestDto loginRequestDto, String authHeader, String refreshTokenFromCookie);
    void logout(String refreshToken);
    LoginResponseDto reissueAccessToken(String refreshToken);
}
