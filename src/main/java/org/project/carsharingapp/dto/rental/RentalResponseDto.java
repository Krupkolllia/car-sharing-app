package org.project.carsharingapp.dto.rental;

import java.time.LocalDate;
import lombok.With;
import org.project.carsharingapp.dto.car.CarResponseDto;

@With
public record RentalResponseDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        CarResponseDto car,
        Long userId
) {}
