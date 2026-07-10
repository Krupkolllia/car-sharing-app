package org.project.carsharingapp.dto.payment.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.project.carsharingapp.dto.payment.PaymentRequestDto;
import org.project.carsharingapp.model.payment.PaymentType;

public record RentalPaymentRequestDto(
        @NotNull
        @Positive
        Long rentalId,

        @NotNull
        PaymentType paymentType
) implements PaymentRequestDto {}
