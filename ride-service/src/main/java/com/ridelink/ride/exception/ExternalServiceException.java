package com.ridelink.ride.exception;

// Thrown when communication with external microservices (e.g., Driver or Payment service) fails.
public class ExternalServiceException extends RuntimeException {

	// Constructs the exception with an error message or underlying cause.
	public ExternalServiceException(String message) {
		super(message);
	}

	public ExternalServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
