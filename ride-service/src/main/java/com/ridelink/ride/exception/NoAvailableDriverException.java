package com.ridelink.ride.exception;

// Thrown when an assignment attempt fails because no active drivers are currently available.
public class NoAvailableDriverException extends RuntimeException {

	// Constructs the exception with details about the unavailable driver scenario.
	public NoAvailableDriverException(String message) {
		super(message);
	}
}
