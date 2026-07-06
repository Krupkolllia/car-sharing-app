package org.project.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.mapper.CarMapper;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    private final CarMapper carMapper;

    public CarResponseDto create(CarRequestDto requestDto) {
        Car car = carMapper.toModel(requestDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Transactional(readOnly = true)
    public Page<CarResponseDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable).map(carMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CarResponseDto findById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find a car with id " + id)
        );

        return carMapper.toDto(car);
    }

    public CarResponseDto update(Long id, CarUpdateRequestDto requestDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find a car with id " + id)
        );

        carMapper.update(car, requestDto);
        return carMapper.toDto(carRepository.save(car));
    }

    public void deleteById(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Cannot find a car with id " + id);
        }

        carRepository.deleteById(id);
    }
}
