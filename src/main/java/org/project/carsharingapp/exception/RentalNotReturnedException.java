package org.project.carsharingapp.exception;

public class RentalNotReturnedException extends RuntimeException {

    public RentalNotReturnedException(String message) {
        super(message);
    }
}
