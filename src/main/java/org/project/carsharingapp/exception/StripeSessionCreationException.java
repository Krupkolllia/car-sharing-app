package org.project.carsharingapp.exception;

public class StripeSessionCreationException extends PaymentGatewayException {
    public StripeSessionCreationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
