package org.project.carsharingapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.model.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilTest {

    private static final String EXPECTED_EXCEPTION_MESSAGE =
        "Authenticated user is missing or principal is not of type User. "
            + "This method should only be called within authenticated endpoints";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("""
        getAuthenticatedUser method with authenticated user should
        return this user
        """)
    void getAuthenticatedUser_WithDomainUserPrincipal_ShouldReturnUser() {
        // Given
        User expected = createTestCustomer();

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                expected,
                null,
                expected.getAuthorities()
            );

        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

        // When
        User actual = SecurityUtil.getAuthenticatedUser();

        // Then
        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("""
        getAuthenticatedUser method without authentication should
        throw IllegalStateException
        """)
    void getAuthenticatedUser_WithoutAuthentication_ShouldThrowIllegalStateException() {
        // Given
        SecurityContextHolder.clearContext();

        // When & Then
        assertThatThrownBy(SecurityUtil::getAuthenticatedUser)
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage(EXPECTED_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("""
        getAuthenticatedUser method with principal of invalid type should
        throw IllegalStateException
        """)
    void getAuthenticatedUser_WithInvalidPrincipalType_ShouldThrowIllegalStateException() {
        // Given
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                "anonymousUser",
                null,
                List.of()
            );

        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

        // When & Then
        assertThatThrownBy(SecurityUtil::getAuthenticatedUser)
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage(EXPECTED_EXCEPTION_MESSAGE);
    }
}
