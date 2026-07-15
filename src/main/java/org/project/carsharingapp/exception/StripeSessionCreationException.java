package org.project.carsharingapp.exception;

public class StripeSessionCreationException extends PaymentProcessingException {
    public StripeSessionCreationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
