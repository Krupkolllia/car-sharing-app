package org.project.carsharingapp.exception.handler;

import java.time.LocalDateTime;
import java.util.Map;

public record ExceptionResponse(
        int status,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> errors
) {}
