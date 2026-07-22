package org.project.carsharingapp.exception;

public class DailyFeeNegativeValueException extends RuntimeException {

    public DailyFeeNegativeValueException(String message) {
        super(message);
    }
}
