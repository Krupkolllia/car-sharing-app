package org.project.carsharingapp.mapper;

import org.mapstruct.Mapping;
import org.project.carsharingapp.dto.payment.PaymentResponseDto;
import org.project.carsharingapp.model.payment.Payment;

public interface PaymentMapper {

    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "userId", source = "rental.user.id")
    PaymentResponseDto toDto(Payment payment);

}
