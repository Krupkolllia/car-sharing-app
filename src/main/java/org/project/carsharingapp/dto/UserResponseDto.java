package org.project.carsharingapp.dto;

import org.project.carsharingapp.model.Role;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role
) {}
