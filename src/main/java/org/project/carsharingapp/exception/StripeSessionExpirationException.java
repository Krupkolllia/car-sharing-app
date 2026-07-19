package org.project.carsharingapp.exception;

public class StripeSessionExpirationException extends PaymentGatewayException {

    public StripeSessionExpirationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
