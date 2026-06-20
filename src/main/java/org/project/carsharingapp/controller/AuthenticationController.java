package org.project.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.AuthResponseDto;
import org.project.carsharingapp.dto.UserLoginRequestDto;
import org.project.carsharingapp.dto.UserRegisterRequestDto;
import org.project.carsharingapp.dto.UserResponseDto;
import org.project.carsharingapp.security.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register new user")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    public UserResponseDto register(@RequestBody @Valid UserRegisterRequestDto requestDto) {
        return authenticationService.register(requestDto);
    }

    @Operation(summary = "Log in")
    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.login(requestDto);
    }

}
