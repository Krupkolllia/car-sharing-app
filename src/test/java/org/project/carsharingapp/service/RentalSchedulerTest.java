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

@ExtendWith(MockitoExtension.class)
public class RentalSchedulerTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    private RentalScheduler rentalScheduler;

    @BeforeEach
    void setUp() {
        rentalScheduler = new RentalScheduler(
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
