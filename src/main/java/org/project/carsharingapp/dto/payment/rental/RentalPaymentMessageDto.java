package org.project.carsharingapp.dto.payment.rental;

import java.math.BigDecimal;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;

public record RentalPaymentMessageDto(
        Long paymentId,
        PaymentType paymentType,
        PaymentStatus paymentStatus,
        BigDecimal total,
        Long userId,
        String userEmail,
        Long rentalId
) {}
