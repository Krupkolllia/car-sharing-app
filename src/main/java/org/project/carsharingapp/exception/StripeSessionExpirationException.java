package org.project.carsharingapp.exception;

public class StripeSessionExpirationException extends PaymentProcessingException {

    public StripeSessionExpirationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
