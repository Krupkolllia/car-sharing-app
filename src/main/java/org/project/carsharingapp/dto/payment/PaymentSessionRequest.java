package org.project.carsharingapp.dto.payment;

import java.math.BigDecimal;
import lombok.With;

@With
public record PaymentSessionRequest(
        BigDecimal amount,
        String currency,
        String productName,
        Long quantity
) {}
