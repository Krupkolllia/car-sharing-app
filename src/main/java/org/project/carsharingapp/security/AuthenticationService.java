package org.project.carsharingapp.security;

import org.project.carsharingapp.dto.AuthResponseDto;
import org.project.carsharingapp.dto.UserLoginRequestDto;
import org.project.carsharingapp.dto.UserRegisterRequestDto;
import org.project.carsharingapp.dto.UserResponseDto;

public interface AuthenticationService {

    UserResponseDto register(UserRegisterRequestDto requestDto);

    AuthResponseDto login(UserLoginRequestDto requestDto);

}
