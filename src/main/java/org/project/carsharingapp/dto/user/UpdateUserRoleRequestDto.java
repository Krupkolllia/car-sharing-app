package org.project.carsharingapp.dto.user;

import jakarta.validation.constraints.NotNull;
import org.project.carsharingapp.model.user.Role;

public record UpdateUserRoleRequestDto(
        @NotNull
        Role role
) {}
