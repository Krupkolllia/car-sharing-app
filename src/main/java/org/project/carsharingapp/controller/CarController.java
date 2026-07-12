package org.project.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.project.carsharingapp.security.annotation.ManagerOnly;
import org.project.carsharingapp.service.CarService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    @Operation(summary = "Add a new car to the catalog")
    @ResponseStatus(HttpStatus.CREATED)
    @ManagerOnly
    @PostMapping
    public CarResponseDto createCar(@Valid @RequestBody CarRequestDto requestDto) {
        return carService.create(requestDto);
    }

    @Operation(summary = "Get a page of cars")
    @GetMapping
    public Page<CarResponseDto> getAll(@ParameterObject Pageable pageable) {
        return carService.findAll(pageable);
    }

    @Operation(summary = "Get a car by id")
    @GetMapping("/{id}")
    public CarResponseDto getCarById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @Operation(summary = "Update (PATCH) a car by id")
    @ManagerOnly
    @PatchMapping("/{id}")
    public CarResponseDto updateCarById(@PathVariable Long id,
                                        @Valid @RequestBody CarUpdateRequestDto requestDto) {
        return carService.update(id, requestDto);
    }

    @Operation(summary = "Delete a car by id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ManagerOnly
    @DeleteMapping("/{id}")
    public void deleteCarById(@PathVariable Long id) {
        carService.deleteById(id);
    }

}
