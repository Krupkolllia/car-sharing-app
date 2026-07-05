package org.project.carsharingapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.config.TestClockConfig;
import org.project.carsharingapp.mapper.RentalMapper;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.service.impl.RentalSchedulerImpl;

@ExtendWith(MockitoExtension.class)
public class RentalSchedulerImplTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    private RentalSchedulerImpl rentalScheduler;

    @BeforeEach
    void setUp() {
        rentalScheduler = new RentalSchedulerImpl(
            rentalRepository,
            rentalMapper,
            notificationService,
            TestClockConfig.FIXED_CLOCK
        );
    }
    
    @Test
    @DisplayName("""
        checkOverdueRentals method in a valid case should
        send message
        """)
    void checkOverdueRentals_With_ShouldSendMessage() {
    }

}
