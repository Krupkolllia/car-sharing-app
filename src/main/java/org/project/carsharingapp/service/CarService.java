package org.project.carsharingapp.service;

import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarResponseDto create(CarRequestDto requestDto);

    Page<CarResponseDto> findAll(Pageable pageable);

    CarResponseDto findById(Long id);

    CarResponseDto update(Long id, CarUpdateRequestDto requestDto);

    void deleteById(Long id);

}
