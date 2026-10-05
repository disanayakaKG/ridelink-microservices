package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ride creation payload requiring passenger and location text. A supplied distance must be
 * positive; @Positive alone does not require a nonnull value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRideRequest {

	@NotBlank(message = "passengerId must not be blank")
	private String passengerId;

	@NotBlank(message = "pickupLocation must not be blank")
	private String pickupLocation;

	@NotBlank(message = "destinationLocation must not be blank")
	private String destinationLocation;

	@Positive(message = "distanceKm must be greater than zero")
	private Double distanceKm;
}
