package org.project.carsharingapp.service.payment.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.project.carsharingapp.config.TestClockConfig;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentCalculationSource;
import org.project.carsharingapp.exception.DailyFeeNegativeValueException;
import org.project.carsharingapp.exception.RentalDurationInvalidException;
import org.project.carsharingapp.exception.RentalReturnedException;
import org.project.carsharingapp.model.payment.PaymentType;

class RentalRegularPaymentCalculatorTest {

    private static final LocalDate RENTAL_DATE = TestClockConfig.FIXED_DATE;

    private static final LocalDate RETURN_DATE =
            TestClockConfig.FIXED_DATE.plusDays(7);

    private RentalPaymentCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RentalRegularPaymentCalculator();
    }

    @Test
    @DisplayName("""
        getSupportedType method always should
        return PaymentType.PAYMENT
        """)
    void getSupportedType_Always_ShouldReturnPaymentTypePayment() {
        assertThat(calculator.getSupportedType())
            .isSameAs(PaymentType.PAYMENT);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "1, 7",
        "39.99, 279.93",
        "40, 280",
        "55.5, 388.5"
    })
    @DisplayName("""
        calculate method in a valid case should
        return calculated result
        """)
    void calculate_ValidCase_ShouldReturnCalculatedResult(String dailyFee, String expected) {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal(dailyFee),
            RENTAL_DATE,
            RETURN_DATE,
            null
        );

        // When
        BigDecimal actual = calculator.calculate(source);

        // Then
        assertThat(actual).isEqualByComparingTo(new BigDecimal(expected));

    }

    @ParameterizedTest
    @ValueSource(strings = {"-10", "-1", "-1.4", "-0.7", "-0.32"})
    @DisplayName("""
        calculate method when rented car's daily fee is negative
        should throw DailyFeeNegativeValueException
        """)
    void calculate_WhenDailyFeeIsNegative_ShouldThrowDailyFeeNegativeValueException(
        String invalidDailyFee
    ) {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal(invalidDailyFee),
            RENTAL_DATE,
            RETURN_DATE,
            null
        );

        // When & Then
        assertThatThrownBy(() -> calculator.calculate(source))
            .isExactlyInstanceOf(DailyFeeNegativeValueException.class)
            .hasMessage("Daily fee must be positive or zero");

    }

    @Test
    @DisplayName("""
        calculate method when rental is returned should
        throw RentalReturnedException
        """)
    void calculate_WhenRentalIsReturned_ShouldThrowRentalReturnedException() {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal("39.99"),
            RENTAL_DATE,
            RETURN_DATE,
            RETURN_DATE
        );

        // When & Then
        assertThatThrownBy(() -> calculator.calculate(source))
            .isExactlyInstanceOf(RentalReturnedException.class)
            .hasMessage("Cannot create rental payment for returned car");

    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 3})
    @DisplayName("""
        calculate method when rental duration is not positive
        should throw RentalDurationInvalidException
        """)
    void calculate_WhenRentalDurationIsNotPositive_ShouldThrowRentalDurationInvalidException(
            long duration
    ) {
        // Given
        var source = new RentalPaymentCalculationSource(
            new BigDecimal("39.99"),
            RENTAL_DATE,
            RENTAL_DATE.minusDays(duration),
            null
        );

        // When & Then
        assertThatThrownBy(() -> calculator.calculate(source))
            .isExactlyInstanceOf(RentalDurationInvalidException.class)
            .hasMessage("Rental duration must be positive");
    }

}
