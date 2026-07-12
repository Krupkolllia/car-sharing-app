package org.project.carsharingapp.dto.user;

import org.project.carsharingapp.model.user.Role;

public record UserProfileResponseDto(
        String email,
        String firstName,
        String lastName,
        Role role
) {}
