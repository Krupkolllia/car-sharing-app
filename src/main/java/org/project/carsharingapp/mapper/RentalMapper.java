package org.project.carsharingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.carsharingapp.config.MapStructConfig;
import org.project.carsharingapp.dto.rental.RentalMessageDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.model.rental.Rental;

@Mapper(config = MapStructConfig.class)
public interface RentalMapper {
    @Mapping(target = "userId", source = "user.id")
    RentalResponseDto toDto(Rental rental);

    @Mapping(target = "rentalId", source = "id")
    @Mapping(target = "customerId", source = "user.id")
    @Mapping(target = "customerEmail", source = "user.email")
    @Mapping(target = "customerFirstName", source = "user.firstName")
    @Mapping(target = "customerLastName", source = "user.lastName")
    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carBrand", source = "car.brand")
    @Mapping(target = "carModel", source = "car.model")
    @Mapping(target = "carType", source = "car.type")
    @Mapping(target = "dailyFee", source = "car.dailyFee")
    @Mapping(target = "availableCars", source = "car.inventory")
    RentalMessageDto toMessageDto(Rental rental);
}
