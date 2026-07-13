package org.project.carsharingapp.exception;

public class InvalidAuthenticationPrincipalException extends RuntimeException {

    public InvalidAuthenticationPrincipalException(String message) {
        super(message);
    }

    public InvalidAuthenticationPrincipalException(String message, Throwable throwable) {
        super(message, throwable);

    }
}
