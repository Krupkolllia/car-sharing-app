package org.project.carsharingapp.exception;

public class StripeSessionRetrievingException extends PaymentGatewayException {

    public StripeSessionRetrievingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
