package com.example.demo.mapper;

import com.example.demo.controller.dto.UserReadDto;
import com.example.demo.domain.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserReadMapper {

    UserReadDto toUserReadDto(UserEntity user);
}
