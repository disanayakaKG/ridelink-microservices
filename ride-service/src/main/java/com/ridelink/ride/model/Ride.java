package com.ridelink.ride.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// MongoDB document entity representing a ride record in the "rides" collection.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rides")
public class Ride {

	// Primary database ID and indexed identifiers for ride, passenger, and driver
	@Id
	private String id;

	@Indexed(unique = true)
	private String rideId;

	@Indexed
	private String passengerId;

	private String driverId;

	// Route pickup and destination locations along with trip distance
	private String pickupLocation;
	private String destinationLocation;
	private Double distanceKm;

	// Ride lifecycle state, fare estimates, and payment identifier
	private RideStatus status;
	private Double estimatedFare;
	private Double finalFare;
	private String paymentId;

	// Timestamp audit fields tracking each lifecycle phase and cancellation details
	private Instant createdAt;
	private Instant updatedAt;
	private Instant assignedAt;
	private Instant acceptedAt;
	private Instant startedAt;
	private Instant completedAt;
	private Instant cancelledAt;
	private String cancellationReason;
}
