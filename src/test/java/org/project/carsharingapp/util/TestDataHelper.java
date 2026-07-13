package org.project.carsharingapp.util;

import static org.project.carsharingapp.config.TestClockConfig.FIXED_DATE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.project.carsharingapp.dto.rental.RentalMessageDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.dto.user.UserProfileResponseDto;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.car.CarType;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;

public class TestDataHelper {
    public static final String ADD_SCRIPT_PATH = "classpath:database/add-test-data.sql";
    public static final String ADD_RENTAL_SCRIPT_PATH = "classpath:database/add-rental-test-data.sql";
    public static final String ADD_PAYMENT_SCRIPT_PATH = "classpath:database/add-payment-test-data.sql";

    public static final Long CAR_ID = 1L;

    public static final Long CUSTOMER_ID = 1L;
    public static final String CUSTOMER_RAW_PASSWORD = "testuser";
    public static final String CUSTOMER_MAIL = "test.user@mail.com";

    public static final Long MANAGER_ID = 2L;
    public static final String MANAGER_MAIL = "test.manager@mail.com";

    public static final LocalDate FIXED_RETURN_DATE = LocalDate.of(2026, 6, 10);

    public static final String NO_OVERDUE_RENTALS_MESSAGE = "No overdue rentals today!";

    public static User createTestCustomer() {
        return new User()
            .setId(CUSTOMER_ID)
            .setEmail(CUSTOMER_MAIL)
            .setPassword("$2a$12$Bx.Pdcm6JggueZYewe1lC.1xWBtMr85se/AnlW4MujOefLH2izXoi")
            .setFirstName("test")
            .setLastName("user")
            .setRole(Role.CUSTOMER)
            .setDeleted(false);
    }

    public static User createTestManager() {
        return new User()
            .setId(MANAGER_ID)
            .setEmail(MANAGER_MAIL)
            .setPassword("$2a$12$vfXgg4N72YCshq0/yEmAEe/LmF7qbpFhP3UgZarqI90bb4OLTZgiG")
            .setFirstName("test")
            .setLastName("manager")
            .setRole(Role.MANAGER)
            .setDeleted(false);
    }

    public static User createAnotherUser() {
        return new User()
            .setEmail("another.user@mail.com")
            .setPassword("$2a$12$TQMKeSp.5LpFuFjf7qDvZeoiMzCPQo1EDQWTVNpXG3LAWnrpPZC5a")
            .setFirstName("another")
            .setLastName("user")
            .setRole(Role.CUSTOMER);
    }

    public static UserProfileResponseDto createTestCustomerProfileResponseDto() {
        return new UserProfileResponseDto(
            CUSTOMER_MAIL, "test", "user", Role.CUSTOMER
        );
    }

    public static CarRequestDto createCarRequestDto() {
        return new CarRequestDto(
            "M5", "BMW", CarType.SEDAN, 1, new BigDecimal("39.99")
        );
    }

    public static CarUpdateRequestDto createCarUpdateRequestDto() {
        return new CarUpdateRequestDto(
            null, null, null, 7, null
        );
    }

    public static CarResponseDto createUpdatedCarResponseDto() {
        return new CarResponseDto(
            1L, "M5", "BMW", CarType.SEDAN,
            7, new BigDecimal("39.99")
        );
    }

    public static Car createCar() {
        return new Car()
            .setId(CAR_ID)
            .setModel("M5")
            .setBrand("BMW")
            .setType(CarType.SEDAN)
            .setInventory(1)
            .setDailyFee(new BigDecimal("39.99"))
            .setDeleted(false);
    }

    public static CarResponseDto createCarResponseDto() {
        return new CarResponseDto(
            null, "M5", "BMW", CarType.SEDAN, 1, new BigDecimal("39.99")
        );
    }

    public static CarResponseDto createCarResponseDtoWithId() {
        return new CarResponseDto(
            1L, "M5", "BMW", CarType.SEDAN, 1, new BigDecimal("39.99")
        );
    }

    public static List<CarResponseDto> createCarResponseDtoList() {
        List<CarResponseDto> responseDtoList = new ArrayList<>();

        responseDtoList.add(new CarResponseDto(
            1L, "M5", "BMW", CarType.SEDAN, 1, new BigDecimal("39.99")
        ));
        responseDtoList.add(new CarResponseDto(
            2L, "RX", "Lexus", CarType.SUV, 3, new BigDecimal("49.99")
        ));
        responseDtoList.add(new CarResponseDto(
            3L, "Civic", "Honda", CarType.HATCHBACK, 5, new BigDecimal("29.99")
        ));

        return responseDtoList;
    }

    public static Rental createRental() {
        return createRental(1L);
    }

    public static Rental createRental(Long id) {
        return new Rental()
            .setId(id)
            .setRentalDate(FIXED_DATE)
            .setReturnDate(FIXED_RETURN_DATE)
            .setActualReturnDate(null)
            .setCar(createCar())
            .setUser(createTestCustomer());
    }

    public static RentalResponseDto createRentalResponseDto() {
        CarResponseDto car = createCarResponseDtoWithId();
        return new RentalResponseDto(
            1L, FIXED_DATE,
            FIXED_RETURN_DATE,
            null, car, CUSTOMER_ID
        );
    }

    public static RentalMessageDto createRentalMessageDto() {
        return createRentalMessageDto(1L);
    }

    public static RentalMessageDto createRentalMessageDto(Long id) {
        return new RentalMessageDto(
            id,
            FIXED_DATE,
            FIXED_RETURN_DATE,
            null,
            CUSTOMER_ID,
            CUSTOMER_MAIL,
            "test",
            "customer",
            CAR_ID,
            "BMW",
            "X5",
            CarType.SEDAN,
            new BigDecimal("39.99"),
            3
        );
    }

    public static Payment createPendingPayment(Long id, Long rentalId) {
        return new Payment()
            .setId(id)
            .setStatus(PaymentStatus.PENDING)
            .setType(PaymentType.PAYMENT)
            .setRental(createRental(rentalId))
            .setSessionUrl("test-session-url")
            .setSessionId("test-session-id")
            .setTotal(new BigDecimal("199.99"));
    }

    public static Payment createPendingPayment() {
        return createPendingPayment(1L, 1L);
    }

    public static List<Payment> createPayments() {
        Rental firstRental = createRental(1L);

        Rental secondRental = new Rental()
            .setId(2L)
            .setRentalDate(FIXED_DATE)
            .setReturnDate(FIXED_RETURN_DATE)
            .setActualReturnDate(FIXED_RETURN_DATE.plusDays(1))
            .setCar(new Car()
                .setId(2L)
                .setModel("RX")
                .setBrand("Lexus")
                .setType(CarType.SUV)
                .setInventory(3)
                .setDailyFee(new BigDecimal("49.99"))
                .setDeleted(false))
            .setUser(createTestCustomer());

        return List.of(
            new Payment()
                .setId(1L)
                .setStatus(PaymentStatus.PENDING)
                .setType(PaymentType.PAYMENT)
                .setRental(firstRental)
                .setSessionUrl("testurl1")
                .setSessionId("testid1")
                .setTotal(new BigDecimal("359.91")),

            new Payment()
                .setId(2L)
                .setStatus(PaymentStatus.PAID)
                .setType(PaymentType.PAYMENT)
                .setRental(secondRental)
                .setSessionUrl("testurl2")
                .setSessionId("testid2")
                .setTotal(new BigDecimal("449.91")),

            new Payment()
                .setId(3L)
                .setStatus(PaymentStatus.PENDING)
                .setType(PaymentType.FINE)
                .setRental(secondRental)
                .setSessionUrl("testurl3")
                .setSessionId("testid3")
                .setTotal(new BigDecimal("64.99"))
        );
    }



}