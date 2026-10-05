package com.ridelink.driver.exception;

// Custom exception thrown when attempting to create a driver profile that already exists
public class DuplicateDriverProfileException extends RuntimeException {

    // Creates the exception with an explanatory error message
    public DuplicateDriverProfileException(String message) {
        super(message);
    }
}