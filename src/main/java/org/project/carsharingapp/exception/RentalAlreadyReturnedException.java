package org.project.carsharingapp.exception;

public class RentalAlreadyReturnedException extends RuntimeException {

    public RentalAlreadyReturnedException(String message) {
        super(message);
    }

    public RentalAlreadyReturnedException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
