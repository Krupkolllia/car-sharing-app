package org.project.carsharingapp.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.carsharingapp.dto.rental.RentalMessageDto;
import org.project.carsharingapp.dto.rental.RentalRequestDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.exception.NoAvailableCarsException;
import org.project.carsharingapp.exception.RentalAlreadyReturnedException;
import org.project.carsharingapp.exception.UnpaidPaymentExistsException;
import org.project.carsharingapp.mapper.RentalMapper;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.CarRepository;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.project.carsharingapp.service.notifications.NotificationRequestedEvent;
import org.project.carsharingapp.service.payment.RentalPaymentService;
import org.project.carsharingapp.util.MessageBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RentalService {

    private static final String NO_OVERDUE_RENTALS_MESSAGE = "No overdue rentals today!";

    private final ApplicationEventPublisher eventPublisher;

    private final RentalPaymentService paymentService;

    private final CarRepository carRepository;

    private final RentalRepository rentalRepository;

    private final RentalMapper rentalMapper;

    private final Clock clock;

    public RentalResponseDto createRental(RentalRequestDto requestDto) {
        User authenticatedUser = SecurityUtil.getAuthenticatedUser();

        if (paymentService.hasUnpaidPayment(authenticatedUser.getId())) {
            throw new UnpaidPaymentExistsException("User has an unpaid payment");
        }

        Long carId = requestDto.carId();

        if (carRepository.decreaseInventory(carId) == 0) {
            if (carRepository.findById(carId).isEmpty()) {
                throw new EntityNotFoundException("Cannot find a car with id " + carId);
            }

            throw new NoAvailableCarsException(
                "This car is not in stock right now. Car id: " + carId);
        }

        Car car = carRepository.findById(carId).get();

        Rental rental = new Rental()
                .setUser(authenticatedUser)
                .setRentalDate(LocalDate.now(clock))
                .setReturnDate(requestDto.returnDate())
                .setCar(car);

        rentalRepository.save(rental);

        RentalMessageDto rentalMessageDto = rentalMapper.toMessageDto(rental);

        publishNotificationRequestedEvent(
                MessageBuilder.buildRentalCreatedMessage(rentalMessageDto)
        );

        log.info(
                "Rental created: rentalId={}, userId={}, carId={}, returnDate={}",
                rental.getId(),
                authenticatedUser.getId(),
                car.getId(),
                rental.getReturnDate()
        );

        return rentalMapper.toDto(rental);
    }

    @Transactional(readOnly = true)
    public Page<RentalResponseDto> findAll(Long userId, Boolean isActive, Pageable pageable) {
        User currentUser = SecurityUtil.getAuthenticatedUser();

        if (currentUser.getRole() == Role.CUSTOMER) {
            userId = currentUser.getId();
        }

        return rentalRepository.findAllByFilters(userId, isActive, pageable)
            .map(rentalMapper::toDto);
    }

    @Transactional(readOnly = true)
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

    public RentalResponseDto returnRental(Long id) {
        Rental rental = rentalRepository.findByIdForUpdate(id).orElseThrow(
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

        Car car = rental.getCar();
        if (carRepository.increaseInventory(car.getId()) == 0) {
            log.error("Increasing car inventory by 1 had no effect."
                    + "Car id={}, rentalId={}, userId={}, car inventory={}",
                    car.getId(),
                    rental.getId(),
                    currentUser.getId(),
                    car.getInventory()
            );

            throw new IllegalStateException(
                "Failed to increase inventory for car with id: " + car.getId()
            );
        }

        Rental updatedRental = rentalRepository.findByIdWithCar(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find a rental with id " + id)
        );

        log.info(
                "Car returned: rentalId={}, returnDate={}, actualReturnDate={}",
                updatedRental.getId(),
                updatedRental.getReturnDate(),
                updatedRental.getActualReturnDate()
        );

        return rentalMapper.toDto(updatedRental);
    }

    @Transactional(readOnly = true)
    public void sendOverdueRentalNotifications() {
        List<Rental> overdueRentals =
                rentalRepository.findAllOverdue(LocalDate.now(clock));

        if (overdueRentals.isEmpty()) {
            publishNotificationRequestedEvent(NO_OVERDUE_RENTALS_MESSAGE);
            return;
        }

        overdueRentals.forEach(rental ->
                publishNotificationRequestedEvent(
                    MessageBuilder.buildOverdueRentalMessage(
                        rentalMapper.toMessageDto(rental))
        ));
    }

    private void publishNotificationRequestedEvent(String message) {
        eventPublisher.publishEvent(
            new NotificationRequestedEvent(message)
        );
    }
}
