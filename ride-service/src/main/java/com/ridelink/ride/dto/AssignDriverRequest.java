package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optional explicit driver selection; an omitted or blank driverId requests selection of the
 * first available driver.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequest {

	/** Optional explicit driver. If omitted, the service selects the first eligible available driver. */
	private String driverId;
}
