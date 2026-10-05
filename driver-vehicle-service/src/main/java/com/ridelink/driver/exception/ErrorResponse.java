package com.ridelink.driver.exception;

import java.time.Instant;

// Standardized response payload returned across the API when an error occurs
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}