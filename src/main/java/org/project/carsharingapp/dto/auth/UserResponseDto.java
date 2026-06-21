package org.project.carsharingapp.dto.auth;

import org.project.carsharingapp.model.user.Role;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role
) {}
