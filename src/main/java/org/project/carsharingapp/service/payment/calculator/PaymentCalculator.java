package org.project.carsharingapp.service.payment.calculator;

import java.math.BigDecimal;
import org.project.carsharingapp.dto.payment.PaymentCalculationSource;
import org.project.carsharingapp.model.payment.PaymentType;

public interface PaymentCalculator<S extends PaymentCalculationSource> {

    PaymentType getSupportedType();

    BigDecimal calculate(S source);

}
