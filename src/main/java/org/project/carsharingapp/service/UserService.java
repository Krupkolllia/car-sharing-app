package org.project.carsharingapp.service;

import org.project.carsharingapp.dto.user.UpdateUserProfileDto;
import org.project.carsharingapp.dto.user.UpdateUserRoleDto;
import org.project.carsharingapp.dto.user.UserProfileDto;

public interface UserService {

    UserProfileDto getProfile();

    UserProfileDto updateProfile(UpdateUserProfileDto updateDto);

    UserProfileDto updateUserRole(Long id, UpdateUserRoleDto updateDto);

}