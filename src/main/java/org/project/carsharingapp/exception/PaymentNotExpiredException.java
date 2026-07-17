package org.project.carsharingapp.exception;

public class PaymentNotExpiredException extends RuntimeException {

    public PaymentNotExpiredException(String message) {
        super(message);
    }
}
