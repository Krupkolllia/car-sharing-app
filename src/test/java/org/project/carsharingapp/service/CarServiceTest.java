package org.project.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.createCar;
import static org.project.carsharingapp.util.TestDataHelper.createCarRequestDto;
import static org.project.carsharingapp.util.TestDataHelper.createCarResponseDto;
import static org.project.carsharingapp.util.TestDataHelper.createCarUpdateRequestDto;
import static org.project.carsharingapp.util.TestDataHelper.createUpdatedCarResponseDto;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.dto.car.CarRequestDto;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.car.CarUpdateRequestDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.mapper.CarMapper;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarService carService;

    @Test
    @DisplayName("""
        create method with valid car request dto should
        return car response dto
        """)
    void create_WithValidRequest_ShouldReturnResponseDto() {
        // Given
        CarRequestDto requestDto = createCarRequestDto();
        Car car = createCar();
        CarResponseDto expected = createCarResponseDto();

        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        // When
        CarResponseDto actual = carService.create(requestDto);

        // Then
        assertThat(actual).isEqualTo(expected);

        verify(carMapper).toModel(requestDto);
        verify(carMapper).toDto(car);
        verify(carRepository).save(car);

        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
        findAll method in valid case should return page
        of car response dtos
        """)
    void findAll_WithValidPageable_ShouldReturnPageOfCarResponseDtos() {
        // Given
        Car car = createCar();
        Pageable pageable = PageRequest.of(0, 10);
        CarResponseDto expected = createCarResponseDto();

        when(carRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(car)));
        when(carMapper.toDto(car)).thenReturn(expected);

        // When
        Page<CarResponseDto> actual = carService.findAll(pageable);

        // Then
        assertThat(actual.getContent())
            .containsExactly(expected);

        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);

        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
        findById method with id of existing car
        should return car response dto
    """)
    void findById_WithValidId_ShouldReturnCarResponseDto() {
        // Given
        Long id = 1L;
        Car car = createCar();
        CarResponseDto expected = createCarResponseDto();

        when(carRepository.findById(id)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expected);

        // When
        CarResponseDto actual = carService.findById(id);

        // Then
        assertThat(actual).isEqualTo(expected);

        verify(carRepository).findById(id);
        verify(carMapper).toDto(car);

        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
        findById method with id of non-existing car
        should throw EntityNotFoundException
        """)
    void findById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidId = 404L;

        when(carRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.findById(invalidId))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a car with id " + invalidId);

        verify(carRepository).findById(invalidId);
        verifyNoMoreInteractions(carRepository);

        verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("""
        update method with id of existing car
        should return updated car response dto
        """)
    void update_WithValidId_ShouldReturnUpdatedCarResponseDto() {
        // Given
        Long id = 1L;
        CarUpdateRequestDto requestDto = createCarUpdateRequestDto();

        Car car = createCar().setId(id);

        CarResponseDto expected = createUpdatedCarResponseDto();

        when(carRepository.findById(id)).thenReturn(Optional.of(car));

        when(carRepository.save(car)).thenReturn(car);

        when(carMapper.toDto(car)).thenReturn(expected);

        // When
        CarResponseDto actual = carService.update(id, requestDto);

        // Then
        assertThat(actual).isEqualTo(expected);

        InOrder inOrder = inOrder(carRepository, carMapper);

        inOrder.verify(carRepository).findById(id);
        inOrder.verify(carMapper).update(car, requestDto);
        inOrder.verify(carRepository).save(car);
        inOrder.verify(carMapper).toDto(car);

        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("""
        update method with id of non-existing car
        should throw EntityNotFoundException
        """)
    void update_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidId = 404L;
        CarUpdateRequestDto requestDto = createCarUpdateRequestDto();

        when(carRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.update(invalidId, requestDto))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a car with id " + invalidId);

        verify(carRepository).findById(invalidId);
        verifyNoMoreInteractions(carRepository);

        verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("""
        deleteById method with id of existing car
        should delete car
    """)
    void deleteById_WithValidId_ShouldDeleteCar() {
        // Given
        Long id = 1L;

        when(carRepository.existsById(id)).thenReturn(true);

        // When
        carService.deleteById(id);

        // Then
        verify(carRepository).existsById(id);
        verify(carRepository).deleteById(id);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("""
    deleteById method with id of non-existing car
    should throw EntityNotFoundException
    """)
    void deleteById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidId = 404L;

        when(carRepository.existsById(invalidId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> carService.deleteById(invalidId))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a car with id " + invalidId);

        verify(carRepository).existsById(invalidId);
        verifyNoMoreInteractions(carRepository);
    }
}
