package org.project.carsharingapp.dto.rental;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RentalRequestDto(
        @NotNull
        @Future
        LocalDate returnDate,

        @NotNull
        Long carId
) {}
