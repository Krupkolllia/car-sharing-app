package org.project.carsharingapp.service.payment.calculator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.project.carsharingapp.dto.payment.PaymentCalculationSource;
import org.project.carsharingapp.exception.UnsupportedPaymentTypeException;
import org.project.carsharingapp.model.payment.PaymentType;

public abstract class AbstractPaymentCalculatorResolver<
        S extends PaymentCalculationSource,
        T extends PaymentCalculator<S>> {

    private final Map<PaymentType, T> calculators;

    protected AbstractPaymentCalculatorResolver(List<T> calculators) {
        this.calculators = calculators.stream()
            .collect(Collectors.toMap(
                PaymentCalculator::getSupportedType,
                Function.identity()
            ));
    }

    public T resolve(PaymentType type) {
        T calculator = calculators.get(type);

        if (calculator == null) {
            throw new UnsupportedPaymentTypeException(type);
        }

        return calculator;
    }

}
