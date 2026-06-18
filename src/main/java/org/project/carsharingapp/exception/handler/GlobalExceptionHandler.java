package org.project.carsharingapp.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.exception.LoginException;
import org.project.carsharingapp.exception.RegistrationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleEntityNotFound(
            EntityNotFoundException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.NOT_FOUND,
            e.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ExceptionResponse> handleRegistrationException(
            RegistrationException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            e.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<ExceptionResponse> handleLoginException(
            LoginException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.UNAUTHORIZED,
            e.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.FORBIDDEN,
            e.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.UNAUTHORIZED,
            e.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(
            MethodArgumentNotValidException e, HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            e.getMessage(),
            request.getRequestURI(),
            errors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleAll(
            Exception e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
            request.getRequestURI(),
            Map.of()
        );
    }

    private ResponseEntity<ExceptionResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return ResponseEntity
            .status(status)
            .body(new ExceptionResponse(
                status.value(),
                message,
                path,
                LocalDateTime.now(),
                errors
            ));
    }

}
