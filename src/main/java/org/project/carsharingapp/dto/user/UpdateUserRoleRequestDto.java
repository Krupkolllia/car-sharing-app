package org.project.carsharingapp.dto.user;

import jakarta.validation.constraints.NotNull;
import org.project.carsharingapp.exception.validation.ValidEnum;
import org.project.carsharingapp.model.user.Role;

public record UpdateUserRoleRequestDto(
        @NotNull
        @ValidEnum(enumClass = Role.class)
        String role
) {}
