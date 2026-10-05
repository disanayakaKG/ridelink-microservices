package com.ridelink.ride.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Standardized error response body returned across all API endpoints upon failure.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	// HTTP error details, timestamp, request path, and optional field validation errors
	private Instant timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
	private List<String> details;
}
