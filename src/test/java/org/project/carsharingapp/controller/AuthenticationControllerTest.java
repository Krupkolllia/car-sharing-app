package org.project.carsharingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_RAW_PASSWORD;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.dto.auth.AuthResponseDto;
import org.project.carsharingapp.dto.auth.UserLoginRequestDto;
import org.project.carsharingapp.dto.auth.UserRegisterRequestDto;
import org.project.carsharingapp.dto.auth.UserResponseDto;
import org.project.carsharingapp.model.user.Role;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

public class AuthenticationControllerTest extends AbstractControllerTest {

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        POST /auth/registration with valid request dto
        should return registered user and status code 201
        """)
    void register_WithValidRequestDto_ShouldReturnRegisteredUserAndStatusCode201()
        throws Exception {
        // Given
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(
            "test.register@mail.com", "testPassword", "test", "test"
        );

        UserResponseDto expected = new UserResponseDto(
            null, "test.register@mail.com", "test", "test", Role.CUSTOMER
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                post("/auth/registration")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isCreated())
            .andReturn();

        // Then
        UserResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), UserResponseDto.class);

        assertThat(actual.id()).isNotNull();

        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        POST /auth/registration with already registered email
        should return status code 409
        """)
    void register_WithRegisteredEmail_ShouldReturnStatusCode409() throws Exception {
        // Given
        String registeredEmail = createTestCustomer().getEmail();
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(
            registeredEmail, "testPassword", "test", "test"
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                post("/auth/registration")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isConflict());
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        POST /auth/login with valid credentials
        should return jwt token and status code 200
        """)
    void login_WithValidCredentials_ShouldReturnJwtTokenAndStatusCode200()
        throws Exception {
        // Given
        String email = createTestCustomer().getEmail();

        UserLoginRequestDto requestDto = new UserLoginRequestDto(
            email, CUSTOMER_RAW_PASSWORD
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        AuthResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), AuthResponseDto.class);

        assertThat(actual.token()).isNotBlank();
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        POST /auth/login with invalid credentials
        should return status code 401
        """)
    void login_WithInvalidCredentials_ShouldReturnStatusCode401() throws Exception {
        // Given
        UserLoginRequestDto invalidRequestDto = new UserLoginRequestDto(
            "invalid.email@mail.com", "invalidPassword"
        );

        String jsonRequest = jsonMapper.writeValueAsString(invalidRequestDto);

        // When & Then
        mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isUnauthorized());
    }
}