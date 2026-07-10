package org.project.carsharingapp.service.payment.calculator;

import java.util.List;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentCalculationSource;
import org.springframework.stereotype.Component;

@Component
public class RentalPaymentCalculatorResolver
        extends AbstractPaymentCalculatorResolver<
                        RentalPaymentCalculationSource,
            RentalPaymentCalculator> {

    public RentalPaymentCalculatorResolver(List<RentalPaymentCalculator> calculators) {
        super(calculators);
    }

}
