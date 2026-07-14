package org.project.carsharingapp.service.payment.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.project.carsharingapp.service.payment.calculator.RentalRegularPaymentCalculatorTest.RENTAL_DATE;
import static org.project.carsharingapp.service.payment.calculator.RentalRegularPaymentCalculatorTest.RETURN_DATE;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentCalculationSource;
import org.project.carsharingapp.exception.DailyFeeNegativeValueException;
import org.project.carsharingapp.exception.RentalNotOverdueException;
import org.project.carsharingapp.exception.RentalNotReturnedException;
import org.project.carsharingapp.model.payment.PaymentType;
import org.project.carsharingapp.properties.PaymentProperties;
import org.project.carsharingapp.properties.PaymentProperties.Stripe;

public class RentalFinePaymentCalculatorTest {

    private RentalPaymentCalculator calculator;

    @BeforeEach
    void setUp() {
        PaymentProperties mockPaymentProperties = new PaymentProperties(
            new BigDecimal("1.3"),
            new Stripe("test-stripe-secret-key")
        );

        calculator = new RentalFinePaymentCalculator(mockPaymentProperties);
    }

    @Test
    @DisplayName("""
        getSupportedType method always should
        return PaymentType.FINE
        """)
    void getSupportedType_With_ShouldReturnPaymentTypeFine() {
        assertThat(calculator.getSupportedType())
            .isSameAs(PaymentType.FINE);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "39.99, 155.961",
        "50, 195",
        "1, 3.9",
        "77.77, 303.303"
    })
    @DisplayName("""
        calculate method in a valid case should
        should return calculated result
        """)
    void calculate_ValidCase_ShouldShouldReturnCalculatedResult(String dailyFee, String expected) {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal(dailyFee),
            RENTAL_DATE,
            RETURN_DATE,
            RETURN_DATE.plusDays(3)
        );

        // When
        BigDecimal actual = calculator.calculate(source);

        // Then
        assertThat(actual).isEqualByComparingTo(new BigDecimal(expected));

    }

    @ParameterizedTest
    @ValueSource(strings = {"-6", "-1", "-1.9", "-0.2", "-0.33", "-5.83721"})
    @DisplayName("""
        calculate method when daily fee is negative should
        throw DailyFeeNegativeValueException
        """)
    void calculate_WhenDailyFeeIsNegative_ShouldThrowDailyFeeNegativeValueException(
        String invalidDailyFee
    ) {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal(invalidDailyFee),
            RENTAL_DATE,
            RETURN_DATE,
            RETURN_DATE.plusDays(3)
        );

        // When & Then
        assertThatThrownBy(() -> calculator.calculate(source))
            .isExactlyInstanceOf(DailyFeeNegativeValueException.class)
            .hasMessage("Daily fee must be positive or zero");

    }

    @Test
    @DisplayName("""
        calculate method when rental is not returned yet should
        throw RentalNotReturnedException
        """)
    void calculate_WhenRentalIsNotReturned_ShouldThrowRentalNotReturnedException() {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal("39.99"),
            RENTAL_DATE,
            RETURN_DATE,
            null
        );

        // When & Then
        assertThatThrownBy(() -> calculator.calculate(source))
            .isExactlyInstanceOf(RentalNotReturnedException.class)
            .hasMessage("Cannot create fine payment before rental is returned");

    }

    @ParameterizedTest
    @ValueSource(longs = {0, 2, 5})
    @DisplayName("""
        calculate method when overdue days amount is not positive should
        throw RentalNotOverdueException
        """)
    void calculate_WhenOverdueDaysAmoundIsNotPositive_ShouldThrowRentalNotOverdueException(
            long duration
    ) {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal("39.99"),
            RENTAL_DATE,
            RETURN_DATE,
            RETURN_DATE.minusDays(duration)
        );

        // When & Then
        assertThatThrownBy(() -> calculator.calculate(source))
            .isExactlyInstanceOf(RentalNotOverdueException.class)
            .hasMessage("Rental is not overdue");

    }

}
