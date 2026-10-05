package com.ridelink.driver.exception;

// Custom exception thrown when a requested driver cannot be found in the system
public class DriverNotFoundException extends RuntimeException {

    // Creates the exception with an explanatory error message
    public DriverNotFoundException(String message) {
        super(message);
    }
}