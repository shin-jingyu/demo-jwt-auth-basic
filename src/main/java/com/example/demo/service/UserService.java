package com.example.demo.service;

import com.example.demo.controller.dto.UserReadDto;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.mapper.UserReadMapper;
import com.example.demo.repository.UserJpaRepository;
import com.example.demo.usecase.UserReadUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserReadUseCase {

    private final UserJpaRepository userJpaRepository;
    private final UserReadMapper userReadMapper;

    @Override
    public UserReadDto readPetFairSummary(String email) {
        var user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "이메일이 존재하지 않습니다."));

        return userReadMapper.toUserReadDto(user);
    }
}
