package com.example.demo.controller.dto;

public record UserReadDto(
        Long id,
        String email,
        String password,
        String nickname
) {
}
