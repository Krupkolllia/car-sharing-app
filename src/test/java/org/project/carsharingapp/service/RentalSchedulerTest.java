package org.project.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RentalSchedulerTest {

    @Mock
    private RentalService rentalService;
    
    @InjectMocks
    private RentalScheduler rentalScheduler;
    
    @Test
    @DisplayName("""
        checkOverdueRentals method should
        delegate overdue rental processing to service
        """)
    void checkOverdueRentals_ShouldDelegateOverdueRentalProcessingToService() {
        // When
        rentalScheduler.checkOverdueRentals();
        
        // Then
        verify(rentalService).sendOverdueRentalNotifications();
        verifyNoMoreInteractions(rentalService);
    
    }
    
    @Test
    @DisplayName("""
        checkOverdueRentals method when service throws exception should
        not throw exception
        """)
    void checkOverdueRentals_WhenServiceThrowsException_ShouldNotThrowException() {
        // Given
        doThrow(new RuntimeException("Rental service exception"))
                .when(rentalService)
                .sendOverdueRentalNotifications();
        
        // When
        assertThatCode(() -> rentalScheduler.checkOverdueRentals())
            .doesNotThrowAnyException();
        
        // Then
        verify(rentalService).sendOverdueRentalNotifications();
        verifyNoMoreInteractions(rentalService);
    
    }

}
