package org.project.carsharingapp.dto.car;

import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        String type,
        Integer inventory,
        BigDecimal dailyFee
) {}
