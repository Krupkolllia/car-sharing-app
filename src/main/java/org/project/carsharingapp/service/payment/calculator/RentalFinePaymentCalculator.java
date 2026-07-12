package org.project.carsharingapp.service.payment.calculator;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentCalculationSource;
import org.project.carsharingapp.exception.PaymentCalculationException;
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
        if (source.actualReturnDate() == null) {
            throw new PaymentCalculationException(
                "Cannot create fine payment before rental is returned"
            );
        }

        long overdueDays = ChronoUnit.DAYS.between(
                source.returnDate(),
                source.actualReturnDate()
        );

        if (overdueDays <= 0) {
            throw new PaymentCalculationException("Rental is not overdue");
        }

        return source.dailyFee()
            .multiply(BigDecimal.valueOf(overdueDays))
            .multiply(paymentProperties.fineMultiplier());
    }
}
