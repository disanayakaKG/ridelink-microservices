package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Request payload for booking a new ride.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRideRequest {

	// Passenger placing the ride request
	@NotBlank(message = "passengerId must not be blank")
	private String passengerId;

	// Origin and destination addresses/landmarks for the journey
	@NotBlank(message = "pickupLocation must not be blank")
	private String pickupLocation;

	@NotBlank(message = "destinationLocation must not be blank")
	private String destinationLocation;

	// Trip distance in kilometers used for fare calculation
	@Positive(message = "distanceKm must be greater than zero")
	private Double distanceKm;
}
