package org.project.carsharingapp.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalPaymentCalculationSource(
        BigDecimal dailyFee,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate
) implements PaymentCalculationSource {}
