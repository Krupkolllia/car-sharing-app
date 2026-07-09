package org.project.carsharingapp.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.project.carsharingapp.exception.validation.ValidEnum;
import org.project.carsharingapp.model.payment.PaymentType;

public record PaymentRequestDto(

        @NotNull
        @Positive
        Long rentalId,

        @NotNull
        @ValidEnum(enumClass = PaymentType.class)
        String paymentType

) {}
