package org.project.carsharingapp.service.payment.calculator;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.project.carsharingapp.dto.payment.RentalPaymentCalculationSource;
import org.project.carsharingapp.exception.PaymentCalculationException;
import org.project.carsharingapp.model.payment.PaymentType;
import org.springframework.stereotype.Component;

@Component
public class RentalRegularPaymentCalculator implements RentalPaymentCalculator {

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.PAYMENT;
    }

    @Override
    public BigDecimal calculate(RentalPaymentCalculationSource source) {
        long rentalDays = ChronoUnit.DAYS.between(
                source.rentalDate(),
                source.returnDate()
        );

        if (rentalDays <= 0) {
            throw new PaymentCalculationException("Rental duration must be positive");
        }

        return source.dailyFee()
            .multiply(BigDecimal.valueOf(rentalDays));
    }
}
