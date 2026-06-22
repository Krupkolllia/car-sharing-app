package org.project.carsharingapp.util;

import java.math.BigDecimal;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.car.CarType;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;

public class TestDataHelper {
    public static final String ADD_SCRIPT_PATH = "classpath:database/add-test-data.sql";
    public static final String DELETE_SCRIPT_PATH = "classpath:database/delete-test-data.sql";

    public static final String USER_RAW_PASSWORD = "testuser1";

    public static User createTestCustomer() {
        return new User()
            .setId(1L)
            .setEmail("test.user1@mail.com")
            .setPassword("$2a$12$KvTBaCc8tnqLRp3F0c1Bp.DZZYUf0TUmLdcNdnt/w2uPdQZ/5l1m6")
            .setFirstName("test")
            .setLastName("user1")
            .setRole(Role.CUSTOMER)
            .setDeleted(false);
    }

    public static CarRequestDto createCarRequestDto() {
        return new CarRequestDto(
            "M5", "BMW", CarType.SEDAN.name(), 1, new BigDecimal("39.99")
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
}