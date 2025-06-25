package com.example.demo.config;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.repository.UserJpaRepository;
import com.example.demo.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DummyUserDate implements ApplicationRunner {

    private final UserJpaRepository userJpaRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<UserEntity> users = new ArrayList<>();
        users.add(UserEntity.builder()
                .email("abc1@naver.com")
                .password(PasswordUtil.encode("1234"))
                .nickname("abc")
                .build());

        users.add(UserEntity.builder()
                .email("aaa2@naver.com")
                .password(PasswordUtil.encode("1111"))
                .nickname("aaa")
                .build());

        users.add(UserEntity.builder()
                .email("ggg3@naver.com")
                .password(PasswordUtil.encode("6666"))
                .nickname("ggg")
                .build());

        userJpaRepository.saveAll(users);
    }
}
