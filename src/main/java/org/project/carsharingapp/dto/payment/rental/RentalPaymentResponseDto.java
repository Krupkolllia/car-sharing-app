package org.project.carsharingapp.dto.payment.rental;

import java.math.BigDecimal;
import lombok.With;
import org.project.carsharingapp.dto.payment.PaymentResponseDto;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;

@With
public record RentalPaymentResponseDto(
        Long id,
        PaymentStatus status,
        PaymentType type,
        Long rentalId,
        Long userId,
        String sessionUrl,
        BigDecimal total
) implements PaymentResponseDto {}
