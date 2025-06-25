package com.example.demo.usecase;

import com.example.demo.controller.dto.UserReadDto;

public interface UserReadUseCase {
    UserReadDto readPetFairSummary(String email);
}
