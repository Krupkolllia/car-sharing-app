package org.project.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_ID;
import static org.project.carsharingapp.util.TestDataHelper.NO_OVERDUE_RENTALS_MESSAGE;
import static org.project.carsharingapp.util.TestDataHelper.createCar;
import static org.project.carsharingapp.util.TestDataHelper.createRental;
import static org.project.carsharingapp.util.TestDataHelper.createRentalMessageDto;
import static org.project.carsharingapp.util.TestDataHelper.createRentalResponseDto;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;
import static org.project.carsharingapp.util.TestDataHelper.createTestManager;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.config.TestClockConfig;
import org.project.carsharingapp.dto.car.CarResponseDto;
import org.project.carsharingapp.dto.rental.RentalMessageDto;
import org.project.carsharingapp.dto.rental.RentalRequestDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.exception.NoAvailableCarsException;
import org.project.carsharingapp.exception.RentalAlreadyReturnedException;
import org.project.carsharingapp.mapper.RentalMapper;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.CarRepository;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.project.carsharingapp.util.MessageBuilder;
import org.project.carsharingapp.util.TestDataHelper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    private RentalService rentalService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService(
            entityManager,
            carRepository,
            rentalRepository,
            rentalMapper,
            notificationService,
            TestClockConfig.FIXED_CLOCK
        );

        authenticatedUser = createTestCustomer();

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getAuthenticatedUser)
            .thenReturn(authenticatedUser);

    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    private void authenticateAsManager() {
        authenticatedUser = createTestManager();
        securityUtilMock.when(SecurityUtil::getAuthenticatedUser)
            .thenReturn(authenticatedUser);
    }
    
    @Test
    @DisplayName("""
        createRental method in a valid case should
        return created rental
        """)
    void createRental_ValidCase_ShouldReturnCreatedRental() {
        // Given
        Car car = createCar();

        RentalRequestDto requestDto = new RentalRequestDto(
            TestDataHelper.FIXED_RETURN_DATE, car.getId()
        );

        Rental expectedRental = createRental();

        RentalResponseDto expected = createRentalResponseDto();

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));

        when(carRepository.decreaseInventory(car.getId())).thenReturn(1);

        when(rentalMapper.toMessageDto(any(Rental.class)))
            .thenReturn(createRentalMessageDto());

        when(rentalMapper.toDto(any(Rental.class))).thenReturn(expected);
    
        // When
        RentalResponseDto actual = rentalService.createRental(requestDto);
    
        // Then
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(rentalCaptor.capture());

        Rental capturedRental = rentalCaptor.getValue();
        assertThat(capturedRental)
            .usingRecursiveComparison()
            .ignoringFields("id", "rentalDate")
            .isEqualTo(expectedRental);

        verify(carRepository).findById(car.getId());
        verify(carRepository).decreaseInventory(car.getId());

        verify(entityManager).refresh(car);

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(notificationService).sendNotification(anyString());

        verify(rentalMapper).toDto(any(Rental.class));
        verify(rentalMapper).toMessageDto(any(Rental.class));

        verifyNoMoreInteractions(carRepository, rentalMapper, entityManager, notificationService);
        securityUtilMock.verifyNoMoreInteractions();

    }
    
    @Test
    @DisplayName("""
        createRental method with id of non-existing car should
        throw EntityNotFoundException
        """)
    void createRental_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidId = 404L;

        RentalRequestDto requestDto = new RentalRequestDto(
            TestDataHelper.FIXED_RETURN_DATE, invalidId
        );

        when(carRepository.findById(invalidId)).thenReturn(Optional.empty());
    
        // When & Then
        assertThatThrownBy(() -> rentalService.createRental(requestDto))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a car with id " + invalidId);

        verify(carRepository).findById(invalidId);
        verifyNoMoreInteractions(carRepository);

        verifyNoInteractions(rentalMapper, entityManager, notificationService);
        securityUtilMock.verifyNoInteractions();
        
    }

    @Test
    @DisplayName("""
        createRental method with no available cars should
        throw NoAvailableCarsException
        """)
    void createRental_WithNoAvailableCars_ShouldThrowNoAvailableException() {
        // Given
        Car car = createCar();

        RentalRequestDto requestDto = new RentalRequestDto(
            TestDataHelper.FIXED_RETURN_DATE, car.getId()
        );

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));

        when(carRepository.decreaseInventory(car.getId())).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> rentalService.createRental(requestDto))
            .isExactlyInstanceOf(NoAvailableCarsException.class)
            .hasMessage(car.getBrand() + " " + car.getModel()
                + " is not in stock right now. Car id: " + car.getId());

        verify(carRepository).findById(car.getId());
        verify(carRepository).decreaseInventory(car.getId());
        verifyNoMoreInteractions(carRepository);

        verifyNoInteractions(rentalMapper, entityManager, notificationService);
        securityUtilMock.verifyNoInteractions();

    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 23L, 999L, Long.MAX_VALUE})
    @DisplayName("""
        findAll method for CUSTOMER should
        return all current user's rentals filtered
        """)
    void findAll_ForCustomer_ShouldReturnAllCurrentUserRentalsFiltered(Long id) {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        RentalResponseDto expected = createRentalResponseDto();

        Rental rental = createRental();

        when(rentalRepository.findAllByFilters(authenticatedUser.getId(), false, pageable))
            .thenReturn(new PageImpl<>(List.of(rental)));

        when(rentalMapper.toDto(rental)).thenReturn(expected);
    
        // When
        RentalResponseDto actual = rentalService.findAll(id, false, pageable)
            .getContent().get(0);
    
        // Then
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(rentalRepository).findAllByFilters(authenticatedUser.getId(), false, pageable);

        verify(rentalMapper).toDto(rental);

        verifyNoMoreInteractions(rentalRepository, rentalMapper);
        securityUtilMock.verifyNoMoreInteractions();

    }
    
    @Test
    @DisplayName("""
        findAll method for MANAGER should
        return all rentals filtered
        """)
    void findAll_ForManager_ShouldReturnAllRentalsFiltered() {
        // Given
        authenticateAsManager();

        Pageable pageable = PageRequest.of(0, 10);
        RentalResponseDto expected = createRentalResponseDto();

        Rental rental = createRental();

        when(rentalRepository.findAllByFilters(CUSTOMER_ID, false, pageable))
            .thenReturn(new PageImpl<>(List.of(rental)));

        when(rentalMapper.toDto(rental)).thenReturn(expected);
    
        // When

        RentalResponseDto actual = rentalService.findAll(CUSTOMER_ID, false, pageable)
            .getContent().get(0);

        // Then
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(rentalRepository).findAllByFilters(CUSTOMER_ID, false, pageable);

        verify(rentalMapper).toDto(rental);

        verifyNoMoreInteractions(rentalRepository, rentalMapper);
        securityUtilMock.verifyNoMoreInteractions();

    }
    
    @Test
    @DisplayName("""
        findById method for MANAGER with id of existing rental
        should return found rental response dto
        """)
    void findById_ForManagerWithValidId_ShouldReturnFoundRentalResponseDto() {
        // Given
        authenticateAsManager();

        Rental rental = createRental();
        RentalResponseDto expected = createRentalResponseDto();

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));

        when(rentalMapper.toDto(rental)).thenReturn(expected);
    
        // When
        RentalResponseDto actual = rentalService.findById(rental.getId());
    
        // Then
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(rentalMapper).toDto(rental);

        verifyNoMoreInteractions(rentalRepository, rentalMapper);
        securityUtilMock.verifyNoMoreInteractions();
        
    }
    
    @Test
    @DisplayName("""
        findById method with id of non-existing rental should
        throw EntityNotFoundException
        """)
    void findById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidId = 404L;

        when(rentalRepository.findByIdWithCar(invalidId))
            .thenReturn(Optional.empty());
    
        // When & Then
        assertThatThrownBy(() -> rentalService.findById(invalidId))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a rental with id " + invalidId);

        verify(rentalRepository).findByIdWithCar(invalidId);
        verifyNoMoreInteractions(rentalRepository);

        verifyNoInteractions(rentalMapper);
        securityUtilMock.verifyNoInteractions();

    }

    @Test
    @DisplayName("""
        findById method for CUSTOMER with id of existing rental
        that belongs to current user should return
        rental response dto
    """)
    void findById_ForCustomerWithOwnRental_ShouldReturnFoundRentalResponseDto() {
        // Given
        Rental rental = createRental();

        RentalResponseDto expected = createRentalResponseDto();

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));

        when((rentalMapper.toDto(rental))).thenReturn(expected);

        // When
        RentalResponseDto actual = rentalService.findById(rental.getId());

        // Then
        assertThat(rental.getUser().getId()).isEqualTo(authenticatedUser.getId());

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(rentalMapper).toDto(rental);

        verifyNoMoreInteractions(rentalRepository, rentalMapper);
        securityUtilMock.verifyNoMoreInteractions();

    }
    
    @Test
    @DisplayName("""
        findById method for CUSTOMER with id of existing rental that
        does not belong to current user should
        throw EntityNotFoundException
        """)
    void findById_ForCustomerWithAnotherUsersRental_ShouldThrowEntityNotFoundException() {
        // Given
        Rental rental = createRental();
        rental.getUser().setId(404L).setEmail("another.customer@mail.com");

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));

        
        // When & Then
        assertThatThrownBy(() -> rentalService.findById(rental.getId()))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a rental with id " + rental.getId());

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verifyNoMoreInteractions(rentalRepository);
        securityUtilMock.verifyNoMoreInteractions();

        verifyNoInteractions(rentalMapper);
        
    }
    
    @Test
    @DisplayName("""
        returnRental method in a valid case for MANAGER
        should mark the rental as returned, increase car inventory,
        and return the updated rental response dto
        """)
    void returnRental_ForManagerValidCase_ShouldReturnUpdatedRentalResponseDto() {
        // Given
        authenticateAsManager();

        Rental rental = createRental();

        RentalResponseDto expected = createRentalResponseDto()
            .withActualReturnDate(LocalDate.now());

        CarResponseDto expectedCar = expected.car();

        expected = expected
            .withCar(expectedCar.withInventory(expectedCar.inventory() + 1));

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));

        when(carRepository.increaseInventory(rental.getCar().getId()))
            .thenReturn(1);

        when(rentalMapper.toDto(rental)).thenReturn(expected);

        // When
        RentalResponseDto actual = rentalService.returnRental(rental.getId());

        // Then
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        assertThat(rental.getActualReturnDate()).isEqualTo(TestClockConfig.FIXED_DATE);

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(carRepository).increaseInventory(rental.getCar().getId());

        verify(entityManager).refresh(rental.getCar());

        verify(rentalMapper).toDto(rental);

        verifyNoMoreInteractions(rentalRepository, carRepository, entityManager, rentalMapper);
        securityUtilMock.verifyNoMoreInteractions();

    }

    @Test
    @DisplayName("""
        returnRental method with id of non-existing rental should
        throw EntityNotFoundException
        """)
    void returnRental_WithInvalidId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidId = 404L;

        when(rentalRepository.findByIdWithCar(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rentalService.returnRental(invalidId))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a rental with id " + invalidId);

        verify(rentalRepository).findByIdWithCar(invalidId);
        verifyNoMoreInteractions(rentalRepository);

        verifyNoInteractions(carRepository, entityManager, rentalMapper);
        securityUtilMock.verifyNoInteractions();

    }

    @Test
    @DisplayName("""
        returnRental method in a valid case for CUSTOMER should
        mark the rental as returned, increase car inventory,
        and return the updated rental response dto
        """)
    void returnRental_ForCustomerValidCase_ShouldReturnUpdatedRentalResponseDto() {
        // Given
        Rental rental = createRental();
        RentalResponseDto expected = createRentalResponseDto()
            .withActualReturnDate(TestClockConfig.FIXED_DATE);

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));

        when(carRepository.increaseInventory(rental.getCar().getId())).thenReturn(1);

        when(rentalMapper.toDto(rental)).thenReturn(expected);

        // When
        RentalResponseDto actual = rentalService.returnRental(rental.getId());

        // Then
        assertThat(rental.getUser().getId()).isEqualTo(authenticatedUser.getId());

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);

        assertThat(rental.getActualReturnDate()).isEqualTo(TestClockConfig.FIXED_DATE);

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(carRepository).increaseInventory(rental.getCar().getId());

        verify(entityManager).refresh(rental.getCar());

        verify(rentalMapper).toDto(rental);

        verifyNoMoreInteractions(rentalRepository, carRepository, entityManager, rentalMapper);
        securityUtilMock.verifyNoMoreInteractions();

    }

    @Test
    @DisplayName("""
        returnRental method for CUSTOMER with id of existing
        rental that does not belong to current user should
        throw EntityNotFoundException
        """)
    void returnRental_ForCustomerWithAnotherUsersRental_ShouldThrowEntityNotFoundException() {
        // Given
        Rental rental = createRental();
        rental.getUser().setId(404L).setEmail("another.customer@mail.com");

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));


        // When & Then
        assertThatThrownBy(() -> rentalService.returnRental(rental.getId()))
            .isExactlyInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cannot find a rental with id " + rental.getId());

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verifyNoMoreInteractions(rentalRepository);
        securityUtilMock.verifyNoMoreInteractions();

        verifyNoInteractions(carRepository, entityManager, rentalMapper);

    }

    @Test
    @DisplayName("""
        returnRental method when rental is already returned should
        throw RentalAlreadyReturnedException
        """)
    void returnRental_WhenRentalIsAlreadyReturned_ShouldThrowRentalAlreadyReturnedException() {
        // Given
        Rental rental = createRental();
        rental.setActualReturnDate(TestClockConfig.FIXED_DATE);

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));

        // When & Then
        assertThatThrownBy(() -> rentalService.returnRental(rental.getId()))
            .isExactlyInstanceOf(RentalAlreadyReturnedException.class)
            .hasMessage("Rental is already returned. Rental id: " + rental.getId());

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verifyNoMoreInteractions(rentalRepository);
        securityUtilMock.verifyNoMoreInteractions();

        verifyNoInteractions(carRepository, entityManager, rentalMapper);

    }

    
    @Test
    @DisplayName("""
        returnRental method when increasing inventory has no effect should
        throw IllegalStateException
        """)
    void returnRental_WhenIncreasingInventoryHasNoEffect_ShouldThrowIllegalStateException() {
        // Given
        Rental rental = createRental();

        when(rentalRepository.findByIdWithCar(rental.getId()))
            .thenReturn(Optional.of(rental));

        when(carRepository.increaseInventory(rental.getCar().getId()))
            .thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> rentalService.returnRental(rental.getId()))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to increase inventory for car with id " + rental.getCar().getId());

        verify(rentalRepository).findByIdWithCar(rental.getId());

        securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

        verify(carRepository).increaseInventory(rental.getCar().getId());

        verifyNoMoreInteractions(rentalRepository, carRepository);
        securityUtilMock.verifyNoMoreInteractions();

        verifyNoInteractions(entityManager, rentalMapper);

    }
    
    @Test
    @DisplayName("""
        sendOverdueRentalNotifications method when overdue
        rentals exist should send message for each rental
        """)
    void sendOverdueRentalNotifications_WithOverdueRentals_ShouldSendMessageForEachRental() {
        // Given
        Rental firstRental = createRental(1L);
        Rental secondRental = createRental(2L);
        Rental thirdRental = createRental(3L);

        RentalMessageDto firstMessage = createRentalMessageDto(1L);
        RentalMessageDto secondMessage = createRentalMessageDto(2L);
        RentalMessageDto thirdMessage = createRentalMessageDto(3L);

        when(rentalRepository.findAllOverdue(TestClockConfig.FIXED_DATE))
            .thenReturn(List.of(firstRental, secondRental, thirdRental));

        when(rentalMapper.toMessageDto(firstRental)).thenReturn(firstMessage);
        when(rentalMapper.toMessageDto(secondRental)).thenReturn(secondMessage);
        when(rentalMapper.toMessageDto(thirdRental)).thenReturn(thirdMessage);

        // When
        rentalService.sendOverdueRentalNotifications();
        
        // Then
        verify(rentalRepository).findAllOverdue(TestClockConfig.FIXED_DATE);

        verify(rentalMapper).toMessageDto(firstRental);
        verify(rentalMapper).toMessageDto(secondRental);
        verify(rentalMapper).toMessageDto(thirdRental);

        verify(notificationService)
            .sendNotification(MessageBuilder.buildOverdueRentalMessage(firstMessage));
        verify(notificationService)
            .sendNotification(MessageBuilder.buildOverdueRentalMessage(secondMessage));
        verify(notificationService)
            .sendNotification(MessageBuilder.buildOverdueRentalMessage(thirdMessage));

        verify(notificationService, never()).sendNotification(NO_OVERDUE_RENTALS_MESSAGE);

        verifyNoMoreInteractions(rentalRepository, rentalMapper, notificationService);
    
    }
    
    @Test
    @DisplayName("""
        sendOverdueRentalNotifications method when no overdue
        rentals should send default notification
        """)
    void sendOverdueRentalNotifications_WithNoOverdueRentals_ShouldSendDefaultNotification() {
        // Given
        when(rentalRepository.findAllOverdue(TestClockConfig.FIXED_DATE))
            .thenReturn(List.of());
        
        // When
        rentalService.sendOverdueRentalNotifications();
        
        // Then
        verify(rentalRepository).findAllOverdue(TestClockConfig.FIXED_DATE);
        verify(notificationService).sendNotification(NO_OVERDUE_RENTALS_MESSAGE);
        
        verifyNoMoreInteractions(rentalRepository, notificationService);
        verifyNoInteractions(rentalMapper);

    }

}
