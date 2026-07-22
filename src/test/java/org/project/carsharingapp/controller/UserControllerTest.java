package org.project.carsharingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_ID;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_MAIL;
import static org.project.carsharingapp.util.TestDataHelper.MANAGER_MAIL;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomerProfileResponseDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import org.project.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import org.project.carsharingapp.dto.user.UserProfileResponseDto;
import org.project.carsharingapp.model.user.Role;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MvcResult;

class UserControllerTest extends AbstractControllerTest {

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        GET /users/me should return response with
        user profile info and status code 200
        """)
    void getUserProfile_ShouldReturnUserProfileInfoAndStatusCode200() throws Exception {
        // Given
        UserProfileResponseDto expected = createTestCustomerProfileResponseDto();

        // When
        MvcResult result = mockMvc.perform(get("/users/me"))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        UserProfileResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), UserProfileResponseDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        PATCH /users/me with valid update request dto should
        return response with updated user profile response dto
        and status code 200
        """)
    void updateProfile_ValidCase_ShouldReturnUpdatedUserAndStatusCode200() throws Exception {
        // Given
        UpdateUserProfileRequestDto requestDto = new UpdateUserProfileRequestDto(
            "updated first name", "updated last name"
        );

        UserProfileResponseDto expected = new UserProfileResponseDto(
            CUSTOMER_MAIL, "updated first name",
            "updated last name", Role.CUSTOMER
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
            patch("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        UserProfileResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), UserProfileResponseDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        PUT /users/{id}/role with id of existing user should
        return response with user having updated role and
        status code 200
        """)
    void updateUserRole_WithValidId_ShouldReturnUpdatedUserAndStatusCode200() throws Exception {
        // Given
        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(Role.MANAGER);

        UserProfileResponseDto expected = new UserProfileResponseDto(
            CUSTOMER_MAIL, "test", "user", requestDto.role()
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
            put("/users/{id}/role", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        UserProfileResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), UserProfileResponseDto.class);

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        PUT /users/{id}/role with id of non-existing user should
        return response with status code 404
        """)
    void updateUserRole_WithInvalidId_ShouldReturnStatusCode404() throws Exception {
        // Given
        Long invalidId = 404L;

        UpdateUserRoleRequestDto requestDto = new UpdateUserRoleRequestDto(Role.MANAGER);

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        mockMvc.perform(
            put("/users/{id}/role", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isNotFound());
    }

}
