package org.project.carsharingapp.exception;

public class StripeSessionRetrievingException extends RuntimeException {

    public StripeSessionRetrievingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
