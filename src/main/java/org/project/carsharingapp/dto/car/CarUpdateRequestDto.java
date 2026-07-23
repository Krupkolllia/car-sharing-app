package org.project.carsharingapp.dto.car;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.project.carsharingapp.model.car.CarType;

public record CarUpdateRequestDto(

        String model,

        String brand,

        CarType type,

        @PositiveOrZero
        Integer inventory,

        @Positive
        @Digits(integer = 10, fraction = 2)
        BigDecimal dailyFee

) {}
