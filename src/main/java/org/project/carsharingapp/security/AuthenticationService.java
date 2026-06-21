package org.project.carsharingapp.security;

import org.project.carsharingapp.dto.auth.AuthResponseDto;
import org.project.carsharingapp.dto.auth.UserLoginRequestDto;
import org.project.carsharingapp.dto.auth.UserRegisterRequestDto;
import org.project.carsharingapp.dto.auth.UserResponseDto;

public interface AuthenticationService {

    UserResponseDto register(UserRegisterRequestDto requestDto);

    AuthResponseDto login(UserLoginRequestDto requestDto);

}
