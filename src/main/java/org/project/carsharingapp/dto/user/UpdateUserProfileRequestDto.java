package org.project.carsharingapp.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserProfileRequestDto(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName
) {}
