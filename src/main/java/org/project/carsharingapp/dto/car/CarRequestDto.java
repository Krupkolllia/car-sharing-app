package org.project.carsharingapp.dto.car;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.project.carsharingapp.model.car.CarType;

public record CarRequestDto(

        @NotBlank
        String model,

        @NotBlank
        String brand,

        @NotNull
        CarType type,

        @NotNull
        @PositiveOrZero
        Integer inventory,

        @NotNull
        @Positive
        @Digits(integer = 10, fraction = 2)
        BigDecimal dailyFee

) {}
