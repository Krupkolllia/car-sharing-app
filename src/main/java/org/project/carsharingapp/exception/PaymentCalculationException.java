package org.project.carsharingapp.exception;

public class PaymentCalculationException extends RuntimeException {

    public PaymentCalculationException(String message) {
        super(message);
    }

    public PaymentCalculationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
