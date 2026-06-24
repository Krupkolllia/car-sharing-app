package org.project.carsharingapp.dto.user;

public record UserProfileResponseDto(
        String email,
        String firstName,
        String lastName,
        String role
) {}
