package org.project.carsharingapp.service.payment.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.exception.UnsupportedPaymentTypeException;
import org.project.carsharingapp.model.payment.PaymentType;

@ExtendWith(MockitoExtension.class)
class RentalPaymentCalculatorResolverTest {

    @Mock
    private RentalPaymentCalculator regularPaymentCalculator;

    @Mock
    private RentalFinePaymentCalculator finePaymentCalculator;

    private RentalPaymentCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        when(regularPaymentCalculator.getSupportedType())
            .thenReturn(PaymentType.PAYMENT);

        when(finePaymentCalculator.getSupportedType())
            .thenReturn(PaymentType.FINE);

        resolver = new RentalPaymentCalculatorResolver(
            List.of(regularPaymentCalculator, finePaymentCalculator)
        );
        
    }
    
    @Test
    @DisplayName("""
        resolve method with Payment type should
        return regular payment calculator
        """)
    void resolve_WithPaymentType_ShouldReturnRegularPaymentCalculator() {
        RentalPaymentCalculator actual =
            resolver.resolve(PaymentType.PAYMENT);

        assertThat(actual).isSameAs(regularPaymentCalculator);

    }

    @Test
    @DisplayName("""
        resolve method with Fine type should
        return fine payment calculator
        """)
    void resolve_WithFineType_ShouldReturnFinePaymentCalculator() {
        RentalPaymentCalculator actual =
            resolver.resolve(PaymentType.FINE);

        assertThat(actual).isSameAs(finePaymentCalculator);

    }

    @Test
    @DisplayName("""
        resolve method with unsupported type should
        throw UnsupportedPaymentTypeException
        """)
    void resolve_WithUnsupportedType_ShouldThrowUnsupportedPaymentTypeException() {
        resolver = new RentalPaymentCalculatorResolver(
            List.of(regularPaymentCalculator)
        );

        assertThatThrownBy(() -> resolver.resolve(PaymentType.FINE))
            .isExactlyInstanceOf(UnsupportedPaymentTypeException.class)
            .hasMessage("Unsupported payment type: " + PaymentType.FINE);

    }
    
    @Test
    @DisplayName("""
        constructor with duplicate supported types should
        throw IllegalStateException
        """)
    void constructor_WithDuplicateSupportedTypes_ShouldThrowIllegalStateException() {
        when(finePaymentCalculator.getSupportedType())
            .thenReturn(PaymentType.PAYMENT);

        assertThatThrownBy(() -> new RentalPaymentCalculatorResolver(
            List.of(regularPaymentCalculator, finePaymentCalculator)
        )).isExactlyInstanceOf(IllegalStateException.class);
    
    }
}
