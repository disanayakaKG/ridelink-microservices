package com.ridelink.ride.dto;

import java.time.Instant;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Data transfer object representing the complete state and details of a ride.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {

	// Database and business identifiers for ride, passenger, and driver
	private String id;
	private String rideId;
	private String passengerId;
	private String driverId;

	// Route locations and distance
	private String pickupLocation;
	private String destinationLocation;
	private Double distanceKm;

	// Lifecycle state, fare amounts, and payment reference
	private RideStatus status;
	private Double estimatedFare;
	private Double finalFare;
	private String paymentId;

	// Lifecycle event timestamps and cancellation details
	private Instant createdAt;
	private Instant updatedAt;
	private Instant assignedAt;
	private Instant acceptedAt;
	private Instant startedAt;
	private Instant completedAt;
	private Instant cancelledAt;
	private String cancellationReason;

	// Converts a Ride entity from the database into an API response DTO.
	public static RideResponse from(Ride ride) {
		return RideResponse.builder()
				.id(ride.getId())
				.rideId(ride.getRideId())
				.passengerId(ride.getPassengerId())
				.driverId(ride.getDriverId())
				.pickupLocation(ride.getPickupLocation())
				.destinationLocation(ride.getDestinationLocation())
				.distanceKm(ride.getDistanceKm())
				.status(ride.getStatus())
				.estimatedFare(ride.getEstimatedFare())
				.finalFare(ride.getFinalFare())
				.paymentId(ride.getPaymentId())
				.createdAt(ride.getCreatedAt())
				.updatedAt(ride.getUpdatedAt())
				.assignedAt(ride.getAssignedAt())
				.acceptedAt(ride.getAcceptedAt())
				.startedAt(ride.getStartedAt())
				.completedAt(ride.getCompletedAt())
				.cancelledAt(ride.getCancelledAt())
				.cancellationReason(ride.getCancellationReason())
				.build();
	}
}
