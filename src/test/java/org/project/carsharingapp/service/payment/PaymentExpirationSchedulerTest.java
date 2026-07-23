package org.project.carsharingapp.service.payment;

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
public class PaymentExpirationSchedulerTest {

    @Mock
    private PaymentExpirationService paymentExpirationService;

    @InjectMocks
    private PaymentExpirationScheduler paymentExpirationScheduler;

    @Test
    @DisplayName("""
        expirePaymentSessions method should
        delegate expiration processing to service
        """)
    void expirePaymentSessions_ShouldDelegateExpirationProcessingToService() {
        // When
        paymentExpirationScheduler.expirePaymentSessions();

        // Then
        verify(paymentExpirationService).markExpiredPayments();
        verifyNoMoreInteractions(paymentExpirationService);

    }

    @Test
    @DisplayName("""
        expirePaymentSessions method when service throws exception should
        not throw exception
        """)
    void expirePaymentSessions_WhenServiceThrowsException_ShouldNotThrowException() {
        // Given
        doThrow(new RuntimeException("Payment expiration service exception"))
            .when(paymentExpirationService)
            .markExpiredPayments();

        // When
        assertThatCode(() -> paymentExpirationScheduler.expirePaymentSessions())
            .doesNotThrowAnyException();

        // Then
        verify(paymentExpirationService).markExpiredPayments();
        verifyNoMoreInteractions(paymentExpirationService);

    }

}
