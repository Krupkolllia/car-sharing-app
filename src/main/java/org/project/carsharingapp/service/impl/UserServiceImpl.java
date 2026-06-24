package org.project.carsharingapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import org.project.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import org.project.carsharingapp.dto.user.UserProfileResponseDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.mapper.UserMapper;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.UserRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.project.carsharingapp.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    @Override
    public UserProfileResponseDto getProfile() {
        return userMapper.toProfileDto(SecurityUtil.getAuthenticatedUser());
    }

    @Override
    public UserProfileResponseDto updateProfile(UpdateUserProfileRequestDto updateDto) {
        User user = SecurityUtil.getAuthenticatedUser();

        userMapper.update(user, updateDto);
        return userMapper.toProfileDto(userRepository.save(user));
    }

    @Override
    public UserProfileResponseDto updateUserRole(Long id, UpdateUserRoleRequestDto updateDto) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find user with id " + id)
        );

        userMapper.update(user, updateDto);
        return userMapper.toProfileDto(user);
    }
}
