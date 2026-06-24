package org.project.carsharingapp.service;

import org.project.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import org.project.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import org.project.carsharingapp.dto.user.UserProfileResponseDto;

public interface UserService {

    UserProfileResponseDto getProfile();

    UserProfileResponseDto updateProfile(UpdateUserProfileRequestDto updateDto);

    UserProfileResponseDto updateUserRole(Long id, UpdateUserRoleRequestDto updateDto);

}
