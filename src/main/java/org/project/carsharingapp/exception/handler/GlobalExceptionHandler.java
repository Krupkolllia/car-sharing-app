package org.project.carsharingapp.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.project.carsharingapp.exception.DailyFeeNegativeValueException;
import org.project.carsharingapp.exception.EmailAlreadyInUseException;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.exception.InvalidAuthenticationPrincipalException;
import org.project.carsharingapp.exception.NoAvailableCarsException;
import org.project.carsharingapp.exception.PaymentNotExpiredException;
import org.project.carsharingapp.exception.PaymentProcessingException;
import org.project.carsharingapp.exception.RentalAlreadyReturnedException;
import org.project.carsharingapp.exception.RentalDurationInvalidException;
import org.project.carsharingapp.exception.RentalNotOverdueException;
import org.project.carsharingapp.exception.RentalNotReturnedException;
import org.project.carsharingapp.exception.RentalReturnedException;
import org.project.carsharingapp.exception.StripeSessionCreationException;
import org.project.carsharingapp.exception.StripeSessionRetrievingException;
import org.project.carsharingapp.exception.UnpaidPaymentExistsException;
import org.project.carsharingapp.exception.UnsupportedPaymentTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
        EmailAlreadyInUseException.class,
        NoAvailableCarsException.class,
        RentalAlreadyReturnedException.class,
        UnpaidPaymentExistsException.class,
        PaymentNotExpiredException.class,
        PaymentProcessingException.class
    })
    public ResponseEntity<ExceptionResponse> handleConflict(
            RuntimeException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.CONFLICT,
            e.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler({
        StripeSessionCreationException.class,
        StripeSessionRetrievingException.class
    })
    public ResponseEntity<ExceptionResponse> handlePayment(
            RuntimeException e, HttpServletRequest request
    ) {
        log.error(
                "Payment service failure. Method: {}, path: {}",
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        return buildResponse(
            HttpStatus.BAD_GATEWAY,
            "Payment service is temporarily unavailable",
            request.getRequestURI()
        );
    }

    @ExceptionHandler({
        InvalidAuthenticationPrincipalException.class,
        AuthenticationException.class
    })
    public ResponseEntity<ExceptionResponse> handleUnauthorized(
            RuntimeException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.UNAUTHORIZED,
            "Authentication required",
            request.getRequestURI()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleEntityNotFound(
            EntityNotFoundException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.NOT_FOUND,
            e.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDenied(
            AccessDeniedException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.FORBIDDEN,
            "Access denied",
            request.getRequestURI()
        );
    }

    @ExceptionHandler({
        UnsupportedPaymentTypeException.class,
        RentalDurationInvalidException.class,
        RentalNotReturnedException.class,
        RentalReturnedException.class,
        RentalNotOverdueException.class,
        DailyFeeNegativeValueException.class
    })
    public ResponseEntity<ExceptionResponse> handleBadRequest(
            RuntimeException e, HttpServletRequest request
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            e.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleAll(
            Exception e, HttpServletRequest request
    ) {
        log.error(
                "Unhandled exception. Method: {}, path: {}",
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            request.getRequestURI()
        );
    }

    private ResponseEntity<ExceptionResponse> buildResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        return ResponseEntity
            .status(status)
            .body(new ExceptionResponse(
                status.value(),
                message,
                path,
                LocalDateTime.now(),
                Map.of()
            ));
    }
}
