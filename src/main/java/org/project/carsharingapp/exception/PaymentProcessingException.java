package org.project.carsharingapp.exception;

public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
