package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Request payload for cancelling an existing ride booking.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRideRequest {

	// Mandatory explanation detailing why the ride is being cancelled
	@NotBlank(message = "reason must not be blank")
	private String reason;
}
