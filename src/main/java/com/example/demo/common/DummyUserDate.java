package com.example.demo.common;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DummyUserDate implements ApplicationRunner {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<UserEntity> users = new ArrayList<>();
        users.add(UserEntity.builder()
                .email("abc1@naver.com")
                .password(passwordEncoder.encode("1234"))
                .nickname("abc")
                .build());

        users.add(UserEntity.builder()
                .email("aaa2@naver.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("aaa")
                .build());

        users.add(UserEntity.builder()
                .email("ggg3@naver.com")
                .password(passwordEncoder.encode("6666"))
                .nickname("ggg")
                .build());

        userJpaRepository.saveAll(users);
    }
}
