package org.project.carsharingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.MANAGER_MAIL;
import static org.project.carsharingapp.util.TestDataHelper.createCarRequestDto;
import static org.project.carsharingapp.util.TestDataHelper.createCarResponseDto;
import static org.project.carsharingapp.util.TestDataHelper.createCarResponseDtoList;
import static org.project.carsharingapp.util.TestDataHelper.createCarResponseDtoWithId;
import static org.project.carsharingapp.util.TestDataHelper.createCarUpdateRequestDto;
import static org.project.carsharingapp.util.TestDataHelper.createUpdatedCarResponseDto;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

class CarControllerTest extends AbstractControllerTest {

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        POST /cars with valid request dto
        should return created car and status code 201
        """)
    void createCar_WithValidRequestDto_ShouldReturnCreatedCarAndStatusCode201()
        throws Exception {
        // Given
        CarRequestDto requestDto = createCarRequestDto();
        CarResponseDto expected = createCarResponseDto();

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                post("/cars")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isCreated())
            .andReturn();

        // Then
        CarResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), CarResponseDto.class);

        assertThat(actual.id()).isNotNull();

        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /cars with valid pageable params
        should return page of cars and status code 200
        """)
    void getAll_WithValidPageable_ShouldReturnPageOfCarsAndStatusCode200()
        throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<CarResponseDto> expected = createCarResponseDtoList();

        // When
        MvcResult result = mockMvc.perform(
                get("/cars")
                    .param("page", String.valueOf(pageable.getPageNumber()))
                    .param("size", String.valueOf(pageable.getPageSize()))
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        String content = jsonMapper
            .readTree(result.getResponse().getContentAsString())
            .get("content")
            .toString();

        CarResponseDto[] actual = jsonMapper.readValue(content, CarResponseDto[].class);

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /cars/{id} with id of existing car
        should return found car and status code 200
        """)
    void getCarById_WithValidId_ShouldReturnFoundCarAndStatusCode200()
        throws Exception {
        // Given
        CarResponseDto expected = createCarResponseDtoWithId();
        Long id = expected.id();

        // When
        MvcResult result = mockMvc.perform(get("/cars/{id}", id))
            .andExpect(status().isOk())
            .andReturn();

        // Then
        CarResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), CarResponseDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /cars/{id} with id of non-existing car
        should return status code 404
        """)
    void getCarById_WithInvalidId_ShouldReturnStatusCode404() throws Exception {
        // Given
        Long invalidId = 404L;

        // When & Then
        mockMvc.perform(get("/cars/{id}", invalidId))
            .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        PATCH /cars/{id} with id of existing car
        should return updated car and status code 200
        """)
    void updateCarById_WithValidId_ShouldReturnUpdatedCarAndStatusCode200()
        throws Exception {
        // Given
        CarUpdateRequestDto updateRequestDto = createCarUpdateRequestDto();

        CarResponseDto expected = createUpdatedCarResponseDto();
        Long id = expected.id();

        String jsonRequest = jsonMapper.writeValueAsString(updateRequestDto);

        // When
        MvcResult result = mockMvc.perform(
                patch("/cars/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        CarResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), CarResponseDto.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        PATCH /cars/{id} with id of non-existing car
        should return status code 404
        """)
    void updateCarById_WithInvalidId_ShouldReturnStatusCode404() throws Exception {
        // Given
        Long invalidId = 404L;
        CarUpdateRequestDto updateRequestDto = createCarUpdateRequestDto();

        String jsonRequest = jsonMapper.writeValueAsString(updateRequestDto);

        // When & Then
        mockMvc.perform(
                patch("/cars/{id}", invalidId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        DELETE /cars/{id} with id of existing car
        should delete car and return status code 204
        """)
    void deleteCarById_WithValidId_ShouldDeleteCarAndReturnStatusCode204()
        throws Exception {
        // Given
        Long id = createCarResponseDtoWithId().id();

        // When & Then
        mockMvc.perform(delete("/cars/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/cars/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        DELETE /cars/{id} with id of non-existing car
        should return status code 404
        """)
    void deleteCarById_WithInvalidId_ShouldReturnStatusCode404() throws Exception {
        // Given
        Long invalidId = 404L;

        // When & Then
        mockMvc.perform(delete("/cars/{id}", invalidId))
            .andExpect(status().isNotFound());
    }
}