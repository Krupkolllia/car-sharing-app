package org.project.carsharingapp.dto.user;

public record UserProfileDto(
        String email,
        String firstName,
        String lastName,
        String role
) {}
