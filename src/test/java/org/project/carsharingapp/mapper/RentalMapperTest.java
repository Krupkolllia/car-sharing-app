package org.project.carsharingapp.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.project.carsharingapp.dto.rental.RentalMessageDto;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.car.CarType;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.User;

public class RentalMapperTest {

    private final RentalMapper rentalMapper = Mappers.getMapper(RentalMapper.class);

    @Test
    @DisplayName("""
    toMessageDto method with valid entity should
    map all fields correctly
    """)
    void toMessageDto_WithValidEntity_ShouldMapAllFieldsCorrectly() {
        // Given
        LocalDate rentalDate = LocalDate.of(2026, 5, 1);
        LocalDate returnDate = LocalDate.of(2026, 5, 5);
        LocalDate actualReturnDate = LocalDate.of(2026, 5, 7);

        User user = new User()
            .setId(1L)
            .setEmail("john.doe@gmail.com")
            .setFirstName("John")
            .setLastName("Doe");

        Car car = new Car()
            .setId(2L)
            .setBrand("BMW")
            .setModel("M5")
            .setType(CarType.SEDAN)
            .setInventory(3)
            .setDailyFee(new BigDecimal("39.99"));

        Rental rental = new Rental()
            .setId(3L)
            .setUser(user)
            .setCar(car)
            .setRentalDate(rentalDate)
            .setReturnDate(returnDate)
            .setActualReturnDate(actualReturnDate);

        // When
        RentalMessageDto actual = rentalMapper.toMessageDto(rental);

        // Then
        assertThat(actual).isNotNull();

        assertThat(actual.rentalId()).isEqualTo(3L);
        assertThat(actual.rentalDate()).isEqualTo(rentalDate);
        assertThat(actual.returnDate()).isEqualTo(returnDate);
        assertThat(actual.actualReturnDate()).isEqualTo(actualReturnDate);

        assertThat(actual.customerId()).isEqualTo(1L);
        assertThat(actual.customerEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(actual.customerFirstName()).isEqualTo("John");
        assertThat(actual.customerLastName()).isEqualTo("Doe");

        assertThat(actual.carId()).isEqualTo(2L);
        assertThat(actual.carBrand()).isEqualTo("BMW");
        assertThat(actual.carModel()).isEqualTo("M5");
        assertThat(actual.carType()).isEqualTo(CarType.SEDAN);
        assertThat(actual.dailyFee()).isEqualByComparingTo(new BigDecimal("39.99"));
        assertThat(actual.availableCars()).isEqualTo(3);
    }

}
