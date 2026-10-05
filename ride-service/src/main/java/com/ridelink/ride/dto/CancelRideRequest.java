package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cancellation payload requiring a nonblank reason; the service separately validates whether
 * the current state permits cancellation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRideRequest {

	@NotBlank(message = "reason must not be blank")
	private String reason;
}
