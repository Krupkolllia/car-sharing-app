package org.project.carsharingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.project.carsharingapp.config.MapStructConfig;
import org.project.carsharingapp.dto.auth.UserRegisterRequestDto;
import org.project.carsharingapp.dto.auth.UserResponseDto;
import org.project.carsharingapp.dto.user.UpdateUserProfileDto;
import org.project.carsharingapp.dto.user.UpdateUserRoleDto;
import org.project.carsharingapp.dto.user.UserProfileDto;
import org.project.carsharingapp.model.user.User;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserResponseDto toDto(User user);

    User toModel(UserRegisterRequestDto requestDto);

    UserProfileDto toProfileDto(User user);

    void update(@MappingTarget User user, UpdateUserProfileDto updateDto);

    void update(@MappingTarget User user, UpdateUserRoleDto updateDto);

}
