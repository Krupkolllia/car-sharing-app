package org.project.carsharingapp.dto.payment.rental;

import java.math.BigDecimal;
import lombok.With;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;

@With
public record RentalPaymentMessageDto(
        Long paymentId,
        PaymentType paymentType,
        PaymentStatus paymentStatus,
        BigDecimal total,
        Long userId,
        String userEmail,
        Long rentalId
) {}
