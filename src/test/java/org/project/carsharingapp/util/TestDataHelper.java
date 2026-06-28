package org.project.carsharingapp.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.project.carsharingapp.dto.user.UserProfileResponseDto;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.car.CarType;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;

public class TestDataHelper {
    public static final String ADD_SCRIPT_PATH = "classpath:database/add-test-data.sql";

    public static final Long CAR_ID = 1L;

    public static final Long CUSTOMER_ID = 1L;
    public static final String CUSTOMER_RAW_PASSWORD = "testuser";
    public static final String CUSTOMER_MAIL = "test.user@mail.com";
    public static final String MANAGER_MAIL = "test.manager@mail.com";

    public static final Long MOCK_USER_ID = 274L;

    public static User createTestCustomer() {
        return new User()
            .setId(1L)
            .setEmail(CUSTOMER_MAIL)
            .setPassword("$2a$12$Bx.Pdcm6JggueZYewe1lC.1xWBtMr85se/AnlW4MujOefLH2izXoi")
            .setFirstName("test")
            .setLastName("user")
            .setRole(Role.CUSTOMER)
            .setDeleted(false);
    }

    public static UserProfileResponseDto createTestCustomerProfileResponseDto() {
        return new UserProfileResponseDto(
            CUSTOMER_MAIL, "test", "user", Role.CUSTOMER.name()
        );
    }

    public static UserProfileResponseDto createTestManagerProfileResponseDto() {
        return new UserProfileResponseDto(
            MANAGER_MAIL, "test", "manager", Role.MANAGER.name()
        );
    }

    public static CarRequestDto createCarRequestDto() {
        return new CarRequestDto(
            "M5", "BMW", CarType.SEDAN.name(), 1, new BigDecimal("39.99")
        );
    }

    public static CarUpdateRequestDto createCarUpdateRequestDto() {
        return new CarUpdateRequestDto(
            null, null, null, 7, null
        );
    }

    public static CarResponseDto createUpdatedCarResponseDto() {
        return new CarResponseDto(
            1L, "M5", "BMW", CarType.SEDAN.name(),
            7, new BigDecimal("39.99")
        );
    }

    public static Car createCar() {
        return new Car()
            .setModel("M5")
            .setBrand("BMW")
            .setType(CarType.SEDAN)
            .setInventory(1)
            .setDailyFee(new BigDecimal("39.99"))
            .setDeleted(false);
    }

    public static CarResponseDto createCarResponseDto() {
        return new CarResponseDto(
            null, "M5", "BMW", CarType.SEDAN.name(), 1, new BigDecimal("39.99")
        );
    }

    public static CarResponseDto createCarResponseDtoWithId() {
        return new CarResponseDto(
            1L, "M5", "BMW", CarType.SEDAN.name(), 1, new BigDecimal("39.99")
        );
    }

    public static List<CarResponseDto> createCarResponseDtoList() {
        List<CarResponseDto> responseDtoList = new ArrayList<>();

        responseDtoList.add(new CarResponseDto(
            1L, "M5", "BMW", CarType.SEDAN.name(), 1, new BigDecimal("39.99")
        ));
        responseDtoList.add(new CarResponseDto(
            2L, "RX", "Lexus", CarType.SUV.name(), 3, new BigDecimal("49.99")
        ));
        responseDtoList.add(new CarResponseDto(
            3L, "Civic", "Honda", CarType.HATCHBACK.name(), 5, new BigDecimal("29.99")
        ));

        return responseDtoList;
    }
}