package org.project.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import org.project.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import org.project.carsharingapp.dto.user.UserProfileResponseDto;
import org.project.carsharingapp.security.annotation.CustomerOnly;
import org.project.carsharingapp.security.annotation.ManagerOnly;
import org.project.carsharingapp.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private static final String ROLE_CUSTOMER = "hasRole('CUSTOMER')";
    private static final String ROLE_MANAGER = "hasRole('MANAGER')";

    private final UserService userService;

    @Operation(summary = "Get my profile info")
    @CustomerOnly
    @GetMapping("/me")
    public UserProfileResponseDto getUserProfile() {
        return userService.getProfile();
    }

    @Operation(summary = "Update profile info")
    @CustomerOnly
    @PatchMapping("/me")
    public UserProfileResponseDto updateProfile(
            @Valid @RequestBody UpdateUserProfileRequestDto updateDto) {
        return userService.updateProfile(updateDto);
    }

    @Operation(summary = "Update user's role")
    @ManagerOnly
    @PutMapping("/{id}/role")
    public UserProfileResponseDto updateUserRole(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequestDto updateDto) {
        return userService.updateUserRole(id, updateDto);
    }

}
