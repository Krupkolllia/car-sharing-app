package org.project.carsharingapp.service.impl;

import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.rental.RentalRequestDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.exception.NoAvailableCarsException;
import org.project.carsharingapp.exception.RentalAlreadyReturnedException;
import org.project.carsharingapp.mapper.RentalMapper;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.CarRepository;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.project.carsharingapp.service.NotificationService;
import org.project.carsharingapp.service.RentalService;
import org.project.carsharingapp.util.TelegramMessageBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final EntityManager entityManager;

    private final CarRepository carRepository;

    private final RentalRepository rentalRepository;

    private final RentalMapper rentalMapper;

    private final NotificationService notificationService;

    private final Clock clock;

    @Override
    public RentalResponseDto createRental(RentalRequestDto requestDto) {
        Long carId = requestDto.carId();

        Car car = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Cannot find a car with id " + carId)
        );

        if (carRepository.decreaseInventory(carId) == 0) {
            throw new NoAvailableCarsException(car.getBrand() + " " + car.getModel()
                + " is not in stock right now. Car id: " + carId);
        }

        entityManager.refresh(car);

        Rental rental = new Rental()
                .setUser(SecurityUtil.getAuthenticatedUser())
                .setRentalDate(LocalDate.now(clock))
                .setReturnDate(requestDto.returnDate())
                .setCar(car);

        rentalRepository.save(rental);

        notificationService.sendNotification(
                TelegramMessageBuilder.buildRentalCreatedMessage(rental));

        return rentalMapper.toDto(rental);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RentalResponseDto> findAll(Long userId, Boolean isActive, Pageable pageable) {
        User currentUser = SecurityUtil.getAuthenticatedUser();

        if (currentUser.getRole() == Role.CUSTOMER) {
            userId = currentUser.getId();
        }

        return rentalRepository.findAllByFilters(userId, isActive, pageable)
            .map(rentalMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public RentalResponseDto findById(Long id) {
        Rental rental = rentalRepository.findByIdWithCar(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find a rental with id " + id)
        );

        User currentUser = SecurityUtil.getAuthenticatedUser();

        if (currentUser.getRole() == Role.CUSTOMER
                && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("Cannot find a rental with id " + id);
        }

        return rentalMapper.toDto(rental);
    }

    @Override
    public RentalResponseDto returnRental(Long id) {
        Rental rental = rentalRepository.findByIdWithCar(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find a rental with id " + id)
        );

        User currentUser = SecurityUtil.getAuthenticatedUser();
        if (currentUser.getRole() == Role.CUSTOMER
                && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new EntityNotFoundException("Cannot find a rental with id " + id);
        }

        if (rental.getActualReturnDate() != null) {
            throw new RentalAlreadyReturnedException(
                "Rental is already returned. Rental id: " + id);
        }

        rental.setActualReturnDate(LocalDate.now(clock));

        if (carRepository.increaseInventory(rental.getCar().getId()) == 0) {
            throw new IllegalStateException(
                "Failed to increase inventory for car with id " + rental.getCar().getId()
            );
        }

        entityManager.refresh(rental.getCar());

        return rentalMapper.toDto(rental);
    }
}
