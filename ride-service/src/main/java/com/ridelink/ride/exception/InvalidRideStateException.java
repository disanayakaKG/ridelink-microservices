package com.ridelink.ride.exception;

// Thrown when an action cannot be performed due to the ride being in an invalid lifecycle status.
public class InvalidRideStateException extends RuntimeException {

	// Constructs the exception with a message detailing the invalid transition.
	public InvalidRideStateException(String message) {
		super(message);
	}
}
