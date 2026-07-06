package org.project.carsharingapp.security;

import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.auth.AuthResponseDto;
import org.project.carsharingapp.dto.auth.UserLoginRequestDto;
import org.project.carsharingapp.dto.auth.UserRegisterRequestDto;
import org.project.carsharingapp.dto.auth.UserResponseDto;
import org.project.carsharingapp.exception.LoginException;
import org.project.carsharingapp.exception.RegistrationException;
import org.project.carsharingapp.mapper.UserMapper;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    public UserResponseDto register(UserRegisterRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException("User already registered with email: "
                + requestDto.email());
        }

        User user = userMapper.toModel(requestDto);

        user.setPassword(passwordEncoder.encode(requestDto.password()));

        user.setRole(Role.CUSTOMER);

        return userMapper.toDto(userRepository.save(user));
    }

    public AuthResponseDto login(UserLoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                requestDto.email(), requestDto.password()
            )
        );

        if (!(authentication.getPrincipal() instanceof UserDetails user)) {
            throw new LoginException("Authentication principal is invalid");
        }

        return new AuthResponseDto(jwtUtil.generateToken(user));
    }
}
