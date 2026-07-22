package org.project.carsharingapp.exception;

public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
