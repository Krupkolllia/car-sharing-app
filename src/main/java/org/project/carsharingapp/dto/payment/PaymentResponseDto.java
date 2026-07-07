package org.project.carsharingapp.dto.payment;

import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        String status,
        String type,
        Long rentalId,
        Long userId,
        String sessionUrl,
        BigDecimal total
) {}
