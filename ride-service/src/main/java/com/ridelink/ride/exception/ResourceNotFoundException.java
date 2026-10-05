package com.ridelink.ride.exception;

// Thrown when a requested entity (e.g., ride by ID) is not found in the database.
public class ResourceNotFoundException extends RuntimeException {

	// Constructs the exception with a message specifying the missing resource.
	public ResourceNotFoundException(String message) {
		super(message);
	}
}
