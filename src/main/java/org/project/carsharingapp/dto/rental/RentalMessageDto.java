package org.project.carsharingapp.dto.rental;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.project.carsharingapp.model.car.CarType;

public record RentalMessageDto(
        Long rentalId,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,

        Long customerId,
        String customerEmail,
        String customerFirstName,
        String customerLastName,

        Long carId,
        String carBrand,
        String carModel,
        CarType carType,
        BigDecimal dailyFee,
        Integer availableCars
) {}
