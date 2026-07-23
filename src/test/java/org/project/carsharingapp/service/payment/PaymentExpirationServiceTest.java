package org.project.carsharingapp.service.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.createPendingPayment;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.exception.PaymentGatewayException;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
public class PaymentExpirationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentExpirationService paymentExpirationService;

    @Test
    @DisplayName("""
        markExpiredPayments method in a valid case should
        mark expired payments
        """)
    void markExpiredPayments_ValidCase_ShouldMarkExpiredPayments() {
        // Given
        Payment firstPendingPayment = createPendingPayment(1L, 1L);
        Payment secondPendingPayment = createPendingPayment(2L, 2L);

        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
            .thenReturn(List.of(firstPendingPayment, secondPendingPayment));

        when(paymentGateway.getStatus(anyString()))
            .thenReturn(PaymentSessionStatus.EXPIRED);

        // When
        paymentExpirationService.markExpiredPayments();

        // Then
        assertThat(firstPendingPayment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(secondPendingPayment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);

        verify(paymentRepository).findAllByStatus(PaymentStatus.PENDING);

        verify(paymentGateway).getStatus(firstPendingPayment.getSessionId());
        verify(paymentGateway).getStatus(secondPendingPayment.getSessionId());

        verify(paymentRepository).saveAll(List.of(firstPendingPayment, secondPendingPayment));

        verifyNoMoreInteractions(paymentRepository, paymentGateway);

    }

    @Test
    @DisplayName("""
        markExpiredPayments method when no expired payments exist should
        not mark any payments
        """)
    void markExpiredPayments_WhenNoExpiredPaymentsExist_ShouldNotMarkAnyPayments() {
        // Given
        Payment firstPendingPayment = createPendingPayment(1L, 1L);
        Payment secondPendingPayment = createPendingPayment(2L, 2L);

        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
            .thenReturn(List.of(firstPendingPayment, secondPendingPayment));

        when(paymentGateway.getStatus(anyString()))
            .thenReturn(PaymentSessionStatus.UNPAID);

        // When
        paymentExpirationService.markExpiredPayments();

        // Then
        assertThat(firstPendingPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(secondPendingPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).findAllByStatus(PaymentStatus.PENDING);

        verify(paymentGateway).getStatus(firstPendingPayment.getSessionId());
        verify(paymentGateway).getStatus(secondPendingPayment.getSessionId());

        verify(paymentRepository, never()).saveAll(anyList());

        verifyNoMoreInteractions(paymentRepository, paymentGateway);

    }

    @Test
    @DisplayName("""
        markExpiredPayments method when no pending payments exist should
        not mark any payments
        """)
    void markExpiredPayments_WhenNoPendingPaymentsExist_ShouldNotMarkAnyPayments() {
        // Given
        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
            .thenReturn(List.of());

        // When
        paymentExpirationService.markExpiredPayments();

        // Then
        verify(paymentRepository).findAllByStatus(PaymentStatus.PENDING);

        verify(paymentRepository, never()).saveAll(anyList());

        verifyNoInteractions(paymentGateway);

        verifyNoMoreInteractions(paymentRepository);

    }

    @Test
    @DisplayName("""
        markExpiredPayments method when exception was thrown should
        not mark any payments
        """)
    void markExpiredPayments_WhenExceptionWasThrown_ShouldNotMarkAnyPayments() {
        // Given
        Payment firstPendingPayment = createPendingPayment(1L, 1L);
        Payment secondPendingPayment = createPendingPayment(2L, 2L);

        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
            .thenReturn(List.of(firstPendingPayment, secondPendingPayment));

        when(paymentGateway.getStatus(anyString()))
            .thenThrow(new PaymentGatewayException("Payment gateway exception"));

        // When
        paymentExpirationService.markExpiredPayments();

        // Then
        assertThat(firstPendingPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(secondPendingPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).findAllByStatus(PaymentStatus.PENDING);

        verify(paymentGateway).getStatus(firstPendingPayment.getSessionId());
        verify(paymentGateway).getStatus(secondPendingPayment.getSessionId());

        verify(paymentRepository, never()).saveAll(anyList());

        verifyNoMoreInteractions(paymentRepository, paymentGateway);

    }

    @Test
    @DisplayName("""
    markExpiredPayments method when one gateway request fails should
    continue processing remaining payments
    """)
    void markExpiredPayments_WhenOneRequestFails_ShouldContinueProcessing() {
        // Given
        Payment failedPayment = createPendingPayment(1L, 1L);
        Payment expiredPayment = createPendingPayment(2L, 2L);

        when(paymentRepository.findAllByStatus(PaymentStatus.PENDING))
            .thenReturn(List.of(failedPayment, expiredPayment));

        when(paymentGateway.getStatus(failedPayment.getSessionId()))
            .thenThrow(new PaymentGatewayException("Payment gateway exception"));

        when(paymentGateway.getStatus(expiredPayment.getSessionId()))
            .thenReturn(PaymentSessionStatus.EXPIRED);

        // When
        paymentExpirationService.markExpiredPayments();

        // Then
        assertThat(failedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(expiredPayment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);

        verify(paymentRepository).findAllByStatus(PaymentStatus.PENDING);

        verify(paymentGateway).getStatus(failedPayment.getSessionId());

        verify(paymentGateway).getStatus(expiredPayment.getSessionId());

        verify(paymentRepository).saveAll(List.of(expiredPayment));

        verifyNoMoreInteractions(paymentRepository, paymentGateway);
    }

}
