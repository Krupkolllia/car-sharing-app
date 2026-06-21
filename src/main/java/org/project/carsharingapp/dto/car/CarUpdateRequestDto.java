package org.project.carsharingapp.dto.car;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.project.carsharingapp.exception.validation.ValidEnum;
import org.project.carsharingapp.model.car.CarType;

public record CarUpdateRequestDto(

        String model,

        String brand,

        @ValidEnum(enumClass = CarType.class)
        String type,

        @PositiveOrZero
        Integer inventory,

        @Positive
        BigDecimal dailyFee

) {}
