package org.project.carsharingapp.dto.payment.rental;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.project.carsharingapp.dto.payment.PaymentCalculationSource;

public record RentalPaymentCalculationSource(
        BigDecimal dailyFee,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate
) implements PaymentCalculationSource {}
