package org.project.carsharingapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JpaUserDetailsService jpaUserDetailsService;

    @Test
    @DisplayName("""
        loadUserByUsername method with existing email
        should return UserDetails
        """)
    void loadUserByUsername_WithExistingEmail_ShouldReturnUserDetails() {
        // Given
        User user = createTestCustomer();
        String email = user.getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        UserDetails actual = jpaUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(actual).isEqualTo(user);

        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("""
        loadUserByUsername method with non-existing email
        should throw UsernameNotFoundException
        """)
    void loadUserByUsername_WithNonExistingEmail_ShouldThrowUsernameNotFoundException() {
        // Given
        String nonExistingEmail = "non.existing@mail.com";

        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername(nonExistingEmail))
            .isExactlyInstanceOf(UsernameNotFoundException.class)
            .hasMessage("Cannot find user by email " + nonExistingEmail);

        verify(userRepository).findByEmail(nonExistingEmail);
        verifyNoMoreInteractions(userRepository);
    }

}
