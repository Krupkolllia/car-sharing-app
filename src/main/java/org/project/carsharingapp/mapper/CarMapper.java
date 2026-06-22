package org.project.carsharingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.project.carsharingapp.config.MapStructConfig;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.project.carsharingapp.model.car.Car;

@Mapper(config = MapStructConfig.class)
public interface CarMapper {

    CarResponseDto toDto(Car car);

    Car toModel(CarRequestDto requestDto);

    void update(@MappingTarget Car car, CarUpdateRequestDto requestDto);

}
