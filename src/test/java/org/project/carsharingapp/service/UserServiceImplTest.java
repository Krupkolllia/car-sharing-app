package org.project.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.createAuthenticatedMockCustomer;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import org.project.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import org.project.carsharingapp.dto.user.UserProfileResponseDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.mapper.UserMapper;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.UserRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.project.carsharingapp.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = createAuthenticatedMockCustomer();

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getAuthenticatedUser)
            .thenReturn(authenticatedUser);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("""
        getProfile method should return user profile
        response dto
        """)
    void getProfile_ShouldReturnCurrentUserProfileResponseDto() {
        // Given
        UserProfileResponseDto expected = new UserProfileResponseDto(
            "authenticated.user@mail.com", null, null, null
        );

        when(userMapper.toProfileDto(authenticatedUser)).thenReturn(expected);

        // When
        UserProfileResponseDto actual = userService.getProfile();

        // Then
        assertThat(actual).isEqualTo(expected);

        verify(userMapper).toProfileDto(authenticatedUser);
        verifyNoMoreInteractions(userMapper);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("""
        updateProfile method with valid update request dto
        should return updated user profile request dto
        """)
    void updateProfile_WithValidRequestDto_ShouldReturnUpdatedUserProfileRequestDto() {
        // Given
        UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto(
            "updated first name", "updated last name"
        );

        UserProfileResponseDto expected = new UserProfileResponseDto(
            "authenticated.user@mail.com", "updated first name",
            "updated last name", Role.CUSTOMER.name()
        );

        when(userRepository.save(authenticatedUser)).thenReturn(authenticatedUser);

        when(userMapper.toProfileDto(authenticatedUser)).thenReturn(expected);

        // When
        UserProfileResponseDto actual = userService.updateProfile(requestDto);

        // Then
        assertThat(actual).isEqualTo(expected);

        InOrder inOrder = Mockito.inOrder(userRepository, userMapper);

        inOrder.verify(userMapper).update(authenticatedUser, requestDto);
        inOrder.verify(userRepository).save(authenticatedUser);
        inOrder.verify(userMapper).toProfileDto(authenticatedUser);

        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("""
        updateUserRole method with id of existing user
        should return updated user profile response dto
        """)
    void updateUserRole_WithValidId_ShouldReturnUpdatedUserProfileResponseDto() {
        // Given
        User user = createTestCustomer();
        Long id = user.getId();

        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(Role.MANAGER.name());

        UserProfileResponseDto expected = new UserProfileResponseDto(
            user.getEmail(), user.getFirstName(), user.getLastName(), requestDto.role()
        );

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        when(userMapper.toProfileDto(user)).thenReturn(expected);

        // When
        UserProfileResponseDto actual = userService.updateUserRole(id, requestDto);

        // Then
        assertThat(actual).isEqualTo(expected);

        InOrder inOrder = Mockito.inOrder(userRepository, userMapper);

        inOrder.verify(userRepository).findById(id);
        inOrder.verify(userMapper).update(user, requestDto);
        inOrder.verify(userMapper).toProfileDto(user);

        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("""
        updateUserRole with id of non-existing user
        should throw EntityNotFoundException
        """)
    void updateUserRole_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidId = 404L;
        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(Role.CUSTOMER.name());

        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserRole(invalidId, requestDto))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find user with id " + invalidId);

        verify(userRepository).findById(invalidId);
        verifyNoMoreInteractions(userRepository);

        verifyNoInteractions(userMapper);

    }
}
