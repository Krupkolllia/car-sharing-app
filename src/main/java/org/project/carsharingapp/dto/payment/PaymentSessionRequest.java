package org.project.carsharingapp.dto.payment;

import java.math.BigDecimal;

public record PaymentSessionRequest(
        BigDecimal amount,
        String currency,
        String productName,
        Long quantity
) {}
