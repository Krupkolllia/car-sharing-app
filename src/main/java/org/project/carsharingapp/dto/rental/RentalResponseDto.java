package org.project.carsharingapp.dto.rental;

import java.time.LocalDate;
import org.project.carsharingapp.dto.car.CarResponseDto;

public record RentalResponseDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        CarResponseDto car,
        Long userId
) {}
