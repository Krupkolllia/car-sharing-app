package org.project.carsharingapp.exception;

public class StripeSessionRetrievingException extends PaymentProcessingException {

    public StripeSessionRetrievingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
