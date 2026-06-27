package org.project.carsharingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.carsharingapp.config.MapStructConfig;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.model.rental.Rental;

@Mapper(config = MapStructConfig.class)
public interface RentalMapper {
    @Mapping(target = "userId", source = "user.id")
    RentalResponseDto toDto(Rental rental);
}
