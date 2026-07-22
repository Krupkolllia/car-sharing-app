package org.project.carsharingapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.model.user.User;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("""
        doFilterInternal method happy path case
        should save new user to SecurityContext
        """)
    void doFilterInternal_HappyPath_ShouldSaveNewUserToSecurityContext()
        throws ServletException, IOException {
        // Given
        String validRawToken = "Bearer valid.token";
        String validToken = "valid.token";

        User user = createTestCustomer();
        String username = user.getUsername();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(validRawToken);

        when(jwtUtil.isValidToken(validToken)).thenReturn(true);

        when(jwtUtil.getUsername(validToken)).thenReturn(username);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();

        assertThat((User) authentication.getPrincipal()).isEqualTo(user);

        assertThat(authentication.getAuthorities()).isEqualTo(user.getAuthorities());

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoMoreInteractions(request);

        verify(jwtUtil).isValidToken(validToken);
        verify(jwtUtil).getUsername(validToken);
        verifyNoMoreInteractions(jwtUtil);

        verify(userDetailsService).loadUserByUsername(username);
        verifyNoMoreInteractions(userDetailsService);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);
    }

    @Test
    @DisplayName("""
        doFilterInternal method with null Authorization header
        should call FilterChain doFilter and not set authentication
    """)
    void doFilterInternal_WithNullAuthorizationHeader_ShouldCallFilterChainDoFilter()
        throws ServletException, IOException {
        // Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoMoreInteractions(request);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);

        verifyNoInteractions(jwtUtil);

        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("""
        doFilterInternal method with blank Authorization header
        should call FilterChain doFilter and not set authentication
    """)
    void doFilterInternal_WithBlankAuthorizationHeader_ShouldCallFilterChainDoFilter()
        throws ServletException, IOException {
        // Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoMoreInteractions(request);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);

        verifyNoInteractions(jwtUtil);

        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("""
        doFilterInternal method with not Bearer Authorization header
        should call FilterChain doFilter and not set authentication
    """)
    void doFilterInternal_WithNotBearerAuthorizationHeader_ShouldCallFilterChainDoFilter()
        throws ServletException, IOException {
        // Given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoMoreInteractions(request);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);

        verifyNoInteractions(jwtUtil);

        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("""
        doFilterInternal method with invalid jwt token from
        authorization header should call FilterChain doFilter
        and not set authentication
        """)
    void doFilterInternal_WithInvalidToken_ShouldCallFilterChainDoFilter()
        throws ServletException, IOException {
        // Given
        String invalidRawToken = "Bearer invalid.jwt.token";
        String invalidToken = "invalid.jwt.token";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(invalidRawToken);

        when(jwtUtil.isValidToken(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoMoreInteractions(request);

        verify(jwtUtil).isValidToken(invalidToken);
        verifyNoMoreInteractions(jwtUtil);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);

        verifyNoInteractions(userDetailsService);

    }

    @Test
    @DisplayName("""
        doFilterInternal method when authentication is already
        in SecurityContext should call FilterChain doFilter
        and not set new authentication
        """)
    void doFilterInternalMethod_WhenAuthenticationIsAlreadySet_ShouldCallFilterChainDoFilter()
        throws ServletException, IOException {
        // Given
        String validRawToken = "Bearer valid.token";
        String validToken = "valid.token";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(validRawToken);

        when(jwtUtil.isValidToken(validToken)).thenReturn(true);

        Authentication existingAuthentication = mock(Authentication.class);

        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication actualAuthentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(actualAuthentication).isSameAs(existingAuthentication);

        verify(request).getHeader(HttpHeaders.AUTHORIZATION);
        verifyNoMoreInteractions(request);

        verify(jwtUtil).isValidToken(validToken);
        verifyNoMoreInteractions(jwtUtil);

        verifyNoInteractions(userDetailsService);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);
    }

}
