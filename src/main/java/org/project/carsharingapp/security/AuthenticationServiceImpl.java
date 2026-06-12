package org.project.carsharingapp.security;

import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.AuthResponseDto;
import org.project.carsharingapp.dto.UserLoginRequestDto;
import org.project.carsharingapp.dto.UserRegisterRequestDto;
import org.project.carsharingapp.dto.UserResponseDto;
import org.project.carsharingapp.exception.LoginException;
import org.project.carsharingapp.exception.RegistrationException;
import org.project.carsharingapp.mapper.UserMapper;
import org.project.carsharingapp.model.Role;
import org.project.carsharingapp.model.User;
import org.project.carsharingapp.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    @Override
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

    @Override
    public AuthResponseDto login(UserLoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                requestDto.email(), requestDto.password()
            )
        );

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new LoginException("Authentication principal is invalid");
        }

        return new AuthResponseDto(jwtUtil.generateToken(user));

    }
}
