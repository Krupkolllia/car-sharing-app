package org.project.carsharingapp.exception;

public class RentalNotOverdueException extends RuntimeException {

    public RentalNotOverdueException(String message) {
        super(message);
    }
}
