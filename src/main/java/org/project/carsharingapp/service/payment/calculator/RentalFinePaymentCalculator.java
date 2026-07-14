package org.project.carsharingapp.service.payment.calculator;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentCalculationSource;
import org.project.carsharingapp.exception.DailyFeeNegativeValueException;
import org.project.carsharingapp.exception.RentalNotOverdueException;
import org.project.carsharingapp.exception.RentalNotReturnedException;
import org.project.carsharingapp.model.payment.PaymentType;
import org.project.carsharingapp.properties.PaymentProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalFinePaymentCalculator implements RentalPaymentCalculator {

    private final PaymentProperties paymentProperties;

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.FINE;
    }

    @Override
    public BigDecimal calculate(RentalPaymentCalculationSource source) {
        if (source.dailyFee().doubleValue() < 0) {
            throw new DailyFeeNegativeValueException(
                "Daily fee must be positive or zero");
        }

        if (source.actualReturnDate() == null) {
            throw new RentalNotReturnedException(
                "Cannot create fine payment before rental is returned"
            );
        }

        long overdueDays = ChronoUnit.DAYS.between(
                source.returnDate(),
                source.actualReturnDate()
        );

        if (overdueDays <= 0) {
            throw new RentalNotOverdueException("Rental is not overdue");
        }

        return source.dailyFee()
            .multiply(BigDecimal.valueOf(overdueDays))
            .multiply(paymentProperties.fineMultiplier());
    }
}
