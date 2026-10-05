package com.ridelink.ride.service.impl;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.PaymentServiceClient;
import com.ridelink.ride.dto.AssignDriverRequest;
import com.ridelink.ride.dto.CancelRideRequest;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.exception.InvalidRideStateException;
import com.ridelink.ride.exception.NoAvailableDriverException;
import com.ridelink.ride.exception.ResourceNotFoundException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.service.RideService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Service implementation managing ride bookings, status lifecycle transitions, and external service coordination.
@Slf4j
@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

	// Set of statuses from which a ride is eligible for cancellation
	private static final Set<RideStatus> CANCELLABLE = EnumSet.of(
			RideStatus.REQUESTED, RideStatus.ASSIGNED, RideStatus.ACCEPTED);

	// Repository and inter-service REST clients
	private final RideRepository rideRepository;
	private final DriverServiceClient driverServiceClient;
	private final PaymentServiceClient paymentServiceClient;

	// Creates a new ride booking, requests initial fare estimate, and stores the ride in REQUESTED state.
	@Override
	public RideResponse createRide(CreateRideRequest request) {
		Instant now = Instant.now();
		String rideId = generateRideId();

		// Calculate fare estimate based on trip distance
		double estimatedFare = paymentServiceClient.estimateFare(request.getDistanceKm());

		// Assemble initial ride record
		Ride ride = Ride.builder()
				.rideId(rideId)
				.passengerId(request.getPassengerId())
				.pickupLocation(request.getPickupLocation())
				.destinationLocation(request.getDestinationLocation())
				.distanceKm(request.getDistanceKm())
				.status(RideStatus.REQUESTED)
				.estimatedFare(estimatedFare)
				.createdAt(now)
				.updatedAt(now)
				.build();

		// Persist new ride to database
		Ride saved = rideRepository.save(ride);
		log.info("Created ride {} for passenger {}", rideId, request.getPassengerId());
		return RideResponse.from(saved);
	}

	// Retrieves a ride by its business identifier (e.g., RIDE001).
	@Override
	public RideResponse getByRideId(String rideId) {
		return RideResponse.from(findByRideIdOrThrow(rideId));
	}

	// Retrieves a ride by its MongoDB document ObjectId.
	@Override
	public RideResponse getByMongoId(String id) {
		Ride ride = rideRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + id));
		return RideResponse.from(ride);
	}

	// Retrieves all rides booked by a specific passenger.
	@Override
	public List<RideResponse> getByPassengerId(String passengerId) {
		return rideRepository.findByPassengerId(passengerId).stream()
				.map(RideResponse::from)
				.toList();
	}

	// Retrieves all rides assigned to a specific driver.
	@Override
	public List<RideResponse> getByDriverId(String driverId) {
		return rideRepository.findByDriverId(driverId).stream()
				.map(RideResponse::from)
				.toList();
	}

	// Matches and assigns an available driver to a REQUESTED ride.
	@Override
	public RideResponse assignDriver(String rideId, AssignDriverRequest request) {
		// Ensure ride exists and is in REQUESTED state
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.REQUESTED, "assign a driver");

		String driverId = request != null ? request.getDriverId() : null;

		// Select driver: auto-pick first available or verify explicitly requested driver
		if (driverId == null || driverId.isBlank()) {
			List<String> available = driverServiceClient.getAvailableDriverIds();
			if (available.isEmpty()) {
				throw new NoAvailableDriverException(
						"No available drivers found for ride " + rideId);
			}
			driverId = available.get(0);
			log.info("Auto-selected driver {} for ride {}", driverId, rideId);
		} else {
			if (!driverServiceClient.isDriverAvailable(driverId)) {
				throw new NoAvailableDriverException(
						"Driver " + driverId + " is not currently available");
			}
		}

		// Update ride entity with driver assignment details and timestamp
		Instant now = Instant.now();
		ride.setDriverId(driverId);
		ride.setStatus(RideStatus.ASSIGNED);
		ride.setAssignedAt(now);
		ride.setUpdatedAt(now);

		Ride saved = rideRepository.save(ride);
		log.info("Assigned driver {} to ride {}", driverId, rideId);
		return RideResponse.from(saved);
	}

	// Transitions an ASSIGNED ride to ACCEPTED status upon driver confirmation.
	@Override
	public RideResponse acceptRide(String rideId) {
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.ASSIGNED, "accept");

		// Record driver acceptance and update timestamp
		Instant now = Instant.now();
		ride.setStatus(RideStatus.ACCEPTED);
		ride.setAcceptedAt(now);
		ride.setUpdatedAt(now);

		return RideResponse.from(rideRepository.save(ride));
	}

	// Transitions an ACCEPTED ride to IN_PROGRESS when the trip starts.
	@Override
	public RideResponse startRide(String rideId) {
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.ACCEPTED, "start");

		// Update status to IN_PROGRESS and record trip start time
		Instant now = Instant.now();
		ride.setStatus(RideStatus.IN_PROGRESS);
		ride.setStartedAt(now);
		ride.setUpdatedAt(now);

		return RideResponse.from(rideRepository.save(ride));
	}

	// Completes an active ride, calculates final fare, and triggers payment creation.
	@Override
	public RideResponse completeRide(String rideId) {
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.IN_PROGRESS, "complete");

		// Calculate final fare based on trip distance and update ride status
		Instant now = Instant.now();
		double finalFare = paymentServiceClient.estimateFare(ride.getDistanceKm());
		ride.setFinalFare(finalFare);
		ride.setStatus(RideStatus.COMPLETED);
		ride.setCompletedAt(now);
		ride.setUpdatedAt(now);

		// Trigger payment transaction with Payment Service (resilient if payment service is down)
		try {
			String paymentId = paymentServiceClient.createPayment(
					ride.getRideId(), ride.getPassengerId(), finalFare);
			ride.setPaymentId(paymentId);
		} catch (Exception ex) {
			log.warn("Payment creation failed for ride {}: {}", rideId, ex.getMessage());
		}

		Ride saved = rideRepository.save(ride);
		log.info("Completed ride {} with final fare {}", rideId, finalFare);
		return RideResponse.from(saved);
	}

	// Cancels a ride if it is currently in REQUESTED, ASSIGNED, or ACCEPTED state.
	@Override
	public RideResponse cancelRide(String rideId, CancelRideRequest request) {
		Ride ride = findByRideIdOrThrow(rideId);

		// Verify ride is in a state where cancellation is permitted
		if (!CANCELLABLE.contains(ride.getStatus())) {
			throw new InvalidRideStateException(
					"Cannot cancel ride " + rideId + " from status " + ride.getStatus()
							+ ". Cancellation is only allowed from REQUESTED, ASSIGNED or ACCEPTED.");
		}

		// Update ride record with cancellation details
		Instant now = Instant.now();
		ride.setStatus(RideStatus.CANCELLED);
		ride.setCancelledAt(now);
		ride.setCancellationReason(request.getReason());
		ride.setUpdatedAt(now);

		return RideResponse.from(rideRepository.save(ride));
	}

	// ── Helper methods ─────────────────────────────────────────────────────────

	// Fetches a ride by its business ID or throws ResourceNotFoundException
	private Ride findByRideIdOrThrow(String rideId) {
		return rideRepository.findByRideId(rideId)
				.orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));
	}

	// Validates that the ride is in the expected status before executing a lifecycle transition
	private void assertStatus(Ride ride, RideStatus expected, String action) {
		if (ride.getStatus() != expected) {
			throw new InvalidRideStateException(
					"Cannot " + action + " ride " + ride.getRideId()
							+ ". Current status is " + ride.getStatus()
							+ "; expected " + expected + ".");
		}
	}

	// Generates a unique business ride ID with format RIDE + 8 uppercase alphanumeric characters
	private String generateRideId() {
		String candidate;
		do {
			candidate = "RIDE" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		} while (rideRepository.existsByRideId(candidate));
		return candidate;
	}
}
