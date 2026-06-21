package org.project.carsharingapp.service.impl;

import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.project.carsharingapp.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class CarServiceImpl implements CarService {

    @Override
    public CarResponseDto create(CarRequestDto requestDto) {
        return null;
    }

    @Override
    public Page<CarResponseDto> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public CarResponseDto findById(Long id) {
        return null;
    }

    @Override
    public CarResponseDto update(Long id, CarUpdateRequestDto requestDto) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
