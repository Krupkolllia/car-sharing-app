package org.project.carsharingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.carsharingapp.config.MapStructConfig;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentMessageDto;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentResponseDto;
import org.project.carsharingapp.model.payment.Payment;

@Mapper(config = MapStructConfig.class)
public interface RentalPaymentMapper extends PaymentMapper<RentalPaymentResponseDto> {

    @Override
    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "userId", source = "rental.user.id")
    RentalPaymentResponseDto toDto(Payment payment);

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "paymentType", source = "type")
    @Mapping(target = "paymentStatus", source = "status")
    @Mapping(target = "userId", source = "rental.user.id")
    @Mapping(target = "userEmail", source = "rental.user.email")
    @Mapping(target = "rentalId", source = "rental.id")
    RentalPaymentMessageDto toMessageDto(Payment payment);

}
