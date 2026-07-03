package org.project.carsharingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.CAR_ID;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_ID;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_MAIL;
import static org.project.carsharingapp.util.TestDataHelper.MANAGER_MAIL;
import static org.project.carsharingapp.util.TestDataHelper.createAnotherUser;
import static org.project.carsharingapp.util.TestDataHelper.createCar;
import static org.project.carsharingapp.util.TestDataHelper.createCarResponseDtoWithId;
import static org.project.carsharingapp.util.TestDataHelper.createRental;
import static org.project.carsharingapp.util.TestDataHelper.createRentalResponseDto;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.project.carsharingapp.config.TestClockConfig;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.rental.RentalRequestDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.mapper.RentalMapper;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.CarRepository;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.repository.UserRepository;
import org.project.carsharingapp.util.TestDataHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import(TestClockConfig.class)
public class RentalControllerTest extends AbstractControllerTest {

    @Autowired
    private RentalMapper rentalMapper;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /rentals in a valid case should
        return created rental and status code 201
        """)
    void createRental_ValidCase_ShouldReturnCreatedRentalAndStatusCode201() throws Exception {
        // Given
        RentalRequestDto requestDto = new RentalRequestDto(TestDataHelper.FIXED_RETURN_DATE, CAR_ID);

        CarResponseDto expectedCar = createCarResponseDtoWithId();
        CarResponseDto expectedCarAfterRental = expectedCar
            .withInventory(expectedCar.inventory() - 1);

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                post("/rentals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isCreated())
            .andReturn();

        // Then
        RentalResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalResponseDto.class);

        assertThat(actual.id()).isNotNull();

        RentalResponseDto expected = new RentalResponseDto(
            actual.id(), TestClockConfig.FIXED_NOW, TestDataHelper.FIXED_RETURN_DATE,
            null, expectedCarAfterRental, CUSTOMER_ID
        );

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        Rental actualRental = rentalRepository.findByIdWithCar(actual.id()).orElseThrow();

        assertThat(actualRental.getRentalDate())
            .isEqualTo(TestClockConfig.FIXED_NOW);

        assertThat(actualRental.getReturnDate())
            .isEqualTo(TestDataHelper.FIXED_RETURN_DATE);

        assertThat(actualRental.getActualReturnDate())
            .isNull();

        assertThat(actualRental.getCar().getId())
            .isEqualTo(expectedCarAfterRental.id());

        assertThat(actualRental.getUser().getId())
            .isEqualTo(expected.userId());
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        POST /rentals with id of non-existing car should
        return 404 response status code
        """)
    void createRental_WithInvalidCarId_ShouldReturnStatusCode404() throws Exception {
        // Given
        RentalRequestDto requestDto = new RentalRequestDto(
            TestDataHelper.FIXED_RETURN_DATE, 404L
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                post("/rentals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isNotFound());

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        POST /rentals when the car has 0 inventory should
        return response status code 409
        """)
    void createRental_WithZeroInventoryCarId_ShouldReturnStatusCode409() throws Exception {
        // Given
        Car car = carRepository.save(createCar().setId(null).setInventory(0));

        RentalRequestDto requestDto = new RentalRequestDto(
            TestDataHelper.FIXED_RETURN_DATE, car.getId()
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(
                post("/rentals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "1, true",
        "-50, false",
        "48, NULL",
        "NULL, false",
        "NULL, NULL",
        "NULL, true",
        "0, true"
    }, nullValues = "NULL")
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        GET /rentals method in a valid case for CUSTOMER should
        return user's own filtered rentals and status code 200
        """)
    void findAll_ForCustomerValidCase_ShouldReturnOwnRentalsAndStatusCode200(Long userId,
        Boolean isActive) throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        List<RentalResponseDto> expected = rentalRepository
            .findAllByFilters(CUSTOMER_ID, isActive, pageable)
            .map(rentalMapper::toDto)
            .toList();

        // When
        MockHttpServletRequestBuilder request = get("/rentals");

        if (userId != null) {
            request.param("user_id", userId.toString());
        }
        if (isActive != null) {
            request.param("is_active", isActive.toString());
        }
        request.param("page", String.valueOf(pageable.getPageNumber()));
        request.param("size", String.valueOf(pageable.getPageSize()));

        MvcResult result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        // Then
        String content = jsonMapper
            .readTree(result.getResponse().getContentAsString())
            .get("content")
            .toString();

        RentalResponseDto[] actual = jsonMapper.readValue(
            content, RentalResponseDto[].class);

        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);

    }

    @ParameterizedTest
    @CsvSource(value = {
        "1, true",
        "-50, false",
        "48, NULL",
        "NULL, false",
        "NULL, NULL",
        "NULL, true",
        "0, true"
    }, nullValues = "NULL")
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        GET /rentals in a valid case for MANAGER should
        return filtered rentals with status code 200
        """)
    void findAll_ForManagerValidCase_ShouldReturnFilteredRentalsWithStatusCode200(Long userId,
        Boolean isActive) throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        List<RentalResponseDto> expected = rentalRepository
            .findAllByFilters(userId, isActive, pageable)
            .map(rentalMapper::toDto)
            .toList();

        // When
        MockHttpServletRequestBuilder request = get("/rentals");

        if (userId != null) {
            request.param("user_id", userId.toString());
        }
        if (isActive != null) {
            request.param("is_active", isActive.toString());
        }
        request.param("page", String.valueOf(pageable.getPageNumber()));
        request.param("size", String.valueOf(pageable.getPageSize()));

        MvcResult result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        // Then
        String content = jsonMapper
            .readTree(result.getResponse().getContentAsString())
            .get("content")
            .toString();

        RentalResponseDto[] actual = jsonMapper.readValue(
            content, RentalResponseDto[].class);

        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        GET /rentals/{id} in a valid case for MANAGER should
        return found rental with status code 200
        """)
    void findById_ForManagerValidCase_ShouldReturnFoundRentalWithStatusCode200() throws Exception {
        // Given
        RentalResponseDto expected = createRentalResponseDto();

        // When
        MvcResult result = mockMvc.perform(
                get("/rentals/{id}", expected.id())
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalResponseDto.class);

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        GET /rentals/{id} method in a valid case for CUSTOMER if found
        rental belongs to authenticated user should
        return rental and status code 200
        """)
    void findById_ForCustomerWithOwnRental_ShouldReturnRentalAndStatusCode200() throws Exception {
        // Given
        RentalResponseDto expected = createRentalResponseDto();

        // When
        MvcResult result = mockMvc.perform(
                get("/rentals/{id}", expected.id())
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalResponseDto.class);

        assertThat(actual.userId()).isEqualTo(CUSTOMER_ID);

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        GET /rentals/{id} with id of non-existing rental should
        return response status code 404
        """)
    void findById_WithInvalidId_ShouldReturnResponseStatusCode404() throws Exception {
        // Given
        Long invalidId = 404L;

        // When & Then
        mockMvc.perform(get("/rentals/{id}", invalidId))
            .andExpect(status().isNotFound());

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        GET /rentals/{id} for CUSTOMER when rental with given
        id does not belong to authenticated user should
        return response status code 404
        """)
    void findById_ForCustomerWithAnotherUsersRental_ShouldReturnResponseStatusCode404()
        throws Exception {
        // Given
        Rental rental = createRental();
        User anotherUser = createAnotherUser();

        userRepository.save(anotherUser);

        rental.setUser(anotherUser);
        rentalRepository.save(rental);

        // When & Then
        mockMvc.perform(get("/rentals/{id}", rental.getId()))
            .andExpect(status().isNotFound());

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        POST /rentals/{id}/return method in a valid case for MANAGER should
        mark the rental as returned, increase car inventory,
        and return the updated rental with status code 200
        """)
    void returnRental_ForManagerValidCase_ShouldReturnUpdatedRentalAndStatusCode200() throws Exception {
        // Given
        RentalResponseDto expected = createRentalResponseDto()
            .withActualReturnDate(TestClockConfig.FIXED_NOW);

        CarResponseDto expectedCar = expected.car();

        expected = expected
            .withCar(expectedCar.withInventory(expectedCar.inventory() + 1));

        // When
        MvcResult result = mockMvc.perform(
                post("/rentals/{id}/return", expected.id())
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalResponseDto.class);

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /rentals/{id}/return method with id of non-existing rental should
        return response status code 404
        """)
    void returnRental_WithInvalidId_ShouldReturnResponseStatusCode404() throws Exception {
        // Given
        Long invalidId = 404L;

        // When & Then
        mockMvc.perform(
                post("/rentals/{id}/return", invalidId)
            )
            .andExpect(status().isNotFound());

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /rentals/{id}/return in a valid case for CUSTOMER should
        mark the rental as returned, increase car inventory,
        and return the updated rental with status code 200
        """)
    void returnRental_ForCustomerValidCase_ShouldReturnUpdatedRentalAndStatusCode200() throws Exception {
        // Given
        RentalResponseDto expected = createRentalResponseDto()
            .withActualReturnDate(TestClockConfig.FIXED_NOW);

        CarResponseDto expectedCar = expected.car();

        expected = expected
            .withCar(expectedCar.withInventory(expectedCar.inventory() + 1));

        // When
        MvcResult result = mockMvc.perform(
                post("/rentals/{id}/return", expected.id())
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalResponseDto.class);

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }
    
    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /rentals/{id}/return for CUSTOMER when rental with given
        id does not belong to authenticated user should
        return response status code 404
        """)
    void returnRental_ForCustomerWithAnotherUsersRental_ShouldReturnResponseStatusCode404() throws Exception {
        // Given
        Rental rental = createRental();
        User anotherUser = createAnotherUser();

        userRepository.save(anotherUser);

        rental.setUser(anotherUser);
        rentalRepository.save(rental);
        
        // When & Then
        mockMvc.perform(post("/rentals/{id}/return", rental.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /rentals/{id}/return when rental is already returned should
        return response status code 409
        """)
    void returnRental_WhenRentalIsAlreadyReturned_ShouldReturnResponseStatusCode409() throws Exception {
        // Given
        Rental rental = createRental().setActualReturnDate(TestDataHelper.FIXED_RETURN_DATE);
        rentalRepository.save(rental);
        
        // When & Then
        mockMvc.perform(post("/rentals/{id}/return", rental.getId()))
            .andExpect(status().isConflict());
    
    }

}
