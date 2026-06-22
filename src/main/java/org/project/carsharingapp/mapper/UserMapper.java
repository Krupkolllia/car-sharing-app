package org.project.carsharingapp.mapper;

import org.mapstruct.Mapper;
import org.project.carsharingapp.config.MapStructConfig;
import org.project.carsharingapp.dto.auth.UserRegisterRequestDto;
import org.project.carsharingapp.dto.auth.UserResponseDto;
import org.project.carsharingapp.model.user.User;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserResponseDto toDto(User user);

    User toModel(UserRegisterRequestDto requestDto);

}
