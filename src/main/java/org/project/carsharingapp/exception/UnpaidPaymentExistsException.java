package org.project.carsharingapp.exception;

public class UnpaidPaymentExistsException extends RuntimeException {

    public UnpaidPaymentExistsException(String message) {
        super(message);
    }
}
