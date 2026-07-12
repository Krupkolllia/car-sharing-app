package org.project.carsharingapp.dto.car;

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

        @PositiveOrZero
        Integer inventory,

        @Positive
        BigDecimal dailyFee

) {}
