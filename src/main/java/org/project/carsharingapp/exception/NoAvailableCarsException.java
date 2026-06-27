package org.project.carsharingapp.exception;

public class NoAvailableCarsException extends RuntimeException {
  public NoAvailableCarsException(String message) {
    super(message);
  }
}
