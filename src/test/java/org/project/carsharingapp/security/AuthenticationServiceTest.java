package org.project.carsharingapp.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.dto.auth.AuthResponseDto;
import org.project.carsharingapp.dto.auth.UserLoginRequestDto;
import org.project.carsharingapp.dto.auth.UserRegisterRequestDto;
import org.project.carsharingapp.dto.auth.UserResponseDto;
import org.project.carsharingapp.exception.InvalidAuthenticationPrincipalException;
import org.project.carsharingapp.exception.EmailAlreadyInUseException;
import org.project.carsharingapp.mapper.UserMapper;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("""
        register method with an unregistered email
        should register a new user and return UserResponseDto
        """)
    void register_WithUnregisteredEmail_ShouldReturnUserResponseDto() {
        // Given
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(
            "new.testuser@mail.com", "rawPassword",
            "test", "user"
        );
        User user = new User()
            .setEmail(requestDto.email())
            .setFirstName(requestDto.firstName())
            .setLastName(requestDto.lastName());

        User registeredUser = new User()
            .setEmail(requestDto.email())
            .setPassword("encodedPassword")
            .setFirstName(requestDto.firstName())
            .setLastName(requestDto.lastName())
            .setRole(Role.CUSTOMER);

        UserResponseDto expected = new UserResponseDto(
            null, registeredUser.getEmail(), registeredUser.getFirstName(),
            registeredUser.getLastName(), registeredUser.getRole()
        );

        when(userRepository.existsByEmail(requestDto.email())).thenReturn(false);

        when(userMapper.toModel(requestDto)).thenReturn(user);

        when(passwordEncoder.encode(requestDto.password())).thenReturn("encodedPassword");

        when(userRepository.save(user)).thenReturn(registeredUser);

        when(userMapper.toDto(registeredUser)).thenReturn(expected);

        // When
        UserResponseDto actual = authenticationService.register(requestDto);

        // Then
        assertThat(actual).isEqualTo(expected);

        verify(userRepository).existsByEmail(requestDto.email());
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);

        verify(userMapper).toModel(requestDto);
        verify(userMapper).toDto(registeredUser);
        verifyNoMoreInteractions(userMapper);

        verify(passwordEncoder).encode(requestDto.password());
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("""
        register method with a registered email
        should throw EmailAlreadyInUseException
        """)
    void register_WithRegisteredEmail_ShouldThrowEmailAlreadyInUseException() {
        // Given
        String registeredEmail = "registered.user@mail.com";

        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(
            registeredEmail, "registeredPassword", "registered", "user"
        );

        when(userRepository.existsByEmail(requestDto.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.register(requestDto))
            .isExactlyInstanceOf(EmailAlreadyInUseException.class)
            .hasMessage("An account with this email already exists");

        verify(userRepository).existsByEmail(requestDto.email());
        verifyNoMoreInteractions(userRepository);

        verifyNoInteractions(userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("""
        login method with valid credentials should
        return AuthResponseDto with jwt token inside
        """)
    void login_WithValidCredentials_ShouldReturnJwtToken() {
        // Given
        String expectedToken = "expectedToken";
        User registeredUser = createTestCustomer();

        UserLoginRequestDto requestDto = new UserLoginRequestDto(
            registeredUser.getEmail(), "rawPassword"
        );

        AuthResponseDto expected = new AuthResponseDto(expectedToken);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(registeredUser);

        when(jwtUtil.generateToken(registeredUser)).thenReturn(expectedToken);

        // When
        AuthResponseDto actual = authenticationService.login(requestDto);

        // Then
        assertThat(actual).isEqualTo(expected);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoMoreInteractions(authenticationManager);

        verify(authentication).getPrincipal();
        verifyNoMoreInteractions(authentication);

        verify(jwtUtil).generateToken(registeredUser);
        verifyNoMoreInteractions(jwtUtil);
    }

    @Test
    @DisplayName("""
        login method with bad credentials should
        throw BadCredentialsException
        """)
    void login_WithBadCredentials_ShouldThrowBadCredentialsException() {
        // Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
            "bad.email@mail.com", "wrongPassword"
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(requestDto))
            .isExactlyInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoMoreInteractions(authenticationManager);

        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("""
        login method when authentication principal is not UserDetails
        should throw InvalidAuthenticationPrincipalException
        """)
    void login_WithNonUserDetailsPrincipal_ShouldThrowInvalidAuthenticationPrincipalException() {
        // Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
            "test.email@mail.com", "testPassword"
        );

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn("Not UserDetails principal");

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(requestDto))
            .isExactlyInstanceOf(InvalidAuthenticationPrincipalException.class)
            .hasMessage("Authentication principal is invalid");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoMoreInteractions(authenticationManager);

        verify(authentication).getPrincipal();
        verifyNoMoreInteractions(authentication);

        verifyNoInteractions(jwtUtil);
    }

}
