package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Request payload for assigning a driver to an existing ride.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequest {

	// Optional explicit driver ID. If omitted, the system automatically selects the first available driver.
	private String driverId;
}
