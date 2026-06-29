package org.project.carsharingapp.dto.rental;

import java.time.LocalDate;
import lombok.With;
import org.project.carsharingapp.dto.car.CarResponseDto;

public record RentalResponseDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        @With
        LocalDate actualReturnDate,
        CarResponseDto car,
        Long userId
) {}
