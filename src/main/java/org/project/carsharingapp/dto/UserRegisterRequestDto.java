package org.project.carsharingapp.dto;

public record UserRegisterRequestDto(
        String email,
        String password,
        String firstName,
        String lastName
) {}
