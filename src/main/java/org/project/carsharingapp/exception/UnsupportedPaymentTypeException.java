package org.project.carsharingapp.exception;

import org.project.carsharingapp.model.payment.PaymentType;

public class UnsupportedPaymentTypeException extends RuntimeException {

    public UnsupportedPaymentTypeException(PaymentType type) {
        super("Unsupported payment type: " + type);
    }

}
