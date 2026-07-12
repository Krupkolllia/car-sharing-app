package org.project.carsharingapp.dto.car;

import java.math.BigDecimal;
import lombok.With;
import org.project.carsharingapp.model.car.CarType;

@With
public record CarResponseDto(
        Long id,
        String model,
        String brand,
        CarType type,
        Integer inventory,
        BigDecimal dailyFee
) {}
