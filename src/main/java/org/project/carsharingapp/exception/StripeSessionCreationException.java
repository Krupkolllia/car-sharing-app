package org.project.carsharingapp.exception;

public class StripeSessionCreationException extends RuntimeException {

    public StripeSessionCreationException(String message) {
        super(message);
    }

    public StripeSessionCreationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
