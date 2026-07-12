package org.project.carsharingapp.mapper;

import org.project.carsharingapp.dto.payment.PaymentResponseDto;
import org.project.carsharingapp.model.payment.Payment;

public interface PaymentMapper<R extends PaymentResponseDto> {
    R toDto(Payment payment);
}
