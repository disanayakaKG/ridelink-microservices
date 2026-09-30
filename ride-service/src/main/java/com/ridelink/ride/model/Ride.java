package com.ridelink.ride.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rides")
public class Ride {

	@Id
	private String id;

	/** Stable business identifier shared with other services (e.g. RIDE001). */
	@Indexed(unique = true)
	private String rideId;

	@Indexed
	private String passengerId;

	private String driverId;

	private String pickupLocation;
	private String destinationLocation;

	/** Distance in kilometres used for fare calculation. */
	private Double distanceKm;

	private RideStatus status;

	private Double estimatedFare;
	private Double finalFare;

	private String paymentId;

	private Instant createdAt;
	private Instant updatedAt;
	private Instant assignedAt;
	private Instant acceptedAt;
	private Instant startedAt;
	private Instant completedAt;
	private Instant cancelledAt;

	private String cancellationReason;
}
