package org.project.carsharingapp.dto.payment.rental;

import java.math.BigDecimal;
import org.project.carsharingapp.dto.payment.PaymentResponseDto;

public record RentalPaymentResponseDto(
        Long id,
        String status,
        String type,
        Long rentalId,
        Long userId,
        String sessionUrl,
        BigDecimal total
) implements PaymentResponseDto {}
