package org.project.carsharingapp.dto.rental;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RentalRequestDto(
        @NotNull
        @FutureOrPresent
        LocalDate returnDate,

        @NotNull
        Long carId
) {}
