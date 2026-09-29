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

@Slf4j
@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

	private static final Set<RideStatus> CANCELLABLE = EnumSet.of(
			RideStatus.REQUESTED, RideStatus.ASSIGNED, RideStatus.ACCEPTED);

	private final RideRepository rideRepository;
	private final DriverServiceClient driverServiceClient;
	private final PaymentServiceClient paymentServiceClient;

	@Override
	public RideResponse createRide(CreateRideRequest request) {
		Instant now = Instant.now();
		String rideId = generateRideId();

		double estimatedFare = paymentServiceClient.estimateFare(request.getDistanceKm());

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

		Ride saved = rideRepository.save(ride);
		log.info("Created ride {} for passenger {}", rideId, request.getPassengerId());
		return RideResponse.from(saved);
	}

	@Override
	public RideResponse getByRideId(String rideId) {
		return RideResponse.from(findByRideIdOrThrow(rideId));
	}

	@Override
	public RideResponse getByMongoId(String id) {
		Ride ride = rideRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + id));
		return RideResponse.from(ride);
	}

	@Override
	public List<RideResponse> getByPassengerId(String passengerId) {
		return rideRepository.findByPassengerId(passengerId).stream()
				.map(RideResponse::from)
				.toList();
	}

	@Override
	public List<RideResponse> getByDriverId(String driverId) {
		return rideRepository.findByDriverId(driverId).stream()
				.map(RideResponse::from)
				.toList();
	}

	@Override
	public RideResponse assignDriver(String rideId, AssignDriverRequest request) {
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.REQUESTED, "assign a driver");

		String driverId = request != null ? request.getDriverId() : null;

		if (driverId == null || driverId.isBlank()) {
			List<String> available = driverServiceClient.getAvailableDriverIds();
			if (available.isEmpty()) {
				throw new NoAvailableDriverException(
						"No available drivers found for ride " + rideId);
			}
			// Simple documented approach: pick the first eligible available driver
			driverId = available.get(0);
			log.info("Auto-selected driver {} for ride {}", driverId, rideId);
		} else {
			if (!driverServiceClient.isDriverAvailable(driverId)) {
				throw new NoAvailableDriverException(
						"Driver " + driverId + " is not currently available");
			}
		}

		Instant now = Instant.now();
		ride.setDriverId(driverId);
		ride.setStatus(RideStatus.ASSIGNED);
		ride.setAssignedAt(now);
		ride.setUpdatedAt(now);

		Ride saved = rideRepository.save(ride);
		log.info("Assigned driver {} to ride {}", driverId, rideId);
		return RideResponse.from(saved);
	}

	@Override
	public RideResponse acceptRide(String rideId) {
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.ASSIGNED, "accept");

		Instant now = Instant.now();
		ride.setStatus(RideStatus.ACCEPTED);
		ride.setAcceptedAt(now);
		ride.setUpdatedAt(now);

		return RideResponse.from(rideRepository.save(ride));
	}

	@Override
	public RideResponse startRide(String rideId) {
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.ACCEPTED, "start");

		Instant now = Instant.now();
		ride.setStatus(RideStatus.IN_PROGRESS);
		ride.setStartedAt(now);
		ride.setUpdatedAt(now);

		return RideResponse.from(rideRepository.save(ride));
	}

	@Override
	public RideResponse completeRide(String rideId) {
		Ride ride = findByRideIdOrThrow(rideId);
		assertStatus(ride, RideStatus.IN_PROGRESS, "complete");

		Instant now = Instant.now();
		// Final fare uses the same documented rule as estimate
		double finalFare = paymentServiceClient.estimateFare(ride.getDistanceKm());
		ride.setFinalFare(finalFare);
		ride.setStatus(RideStatus.COMPLETED);
		ride.setCompletedAt(now);
		ride.setUpdatedAt(now);

		try {
			String paymentId = paymentServiceClient.createPayment(
					ride.getRideId(), ride.getPassengerId(), finalFare);
			ride.setPaymentId(paymentId);
		} catch (Exception ex) {
			// Persist completion even if payment service is temporarily down;
			// paymentId can be reconciled later.
			log.warn("Payment creation failed for ride {}: {}", rideId, ex.getMessage());
		}

		Ride saved = rideRepository.save(ride);
		log.info("Completed ride {} with final fare {}", rideId, finalFare);
		return RideResponse.from(saved);
	}

	@Override
	public RideResponse cancelRide(String rideId, CancelRideRequest request) {
		Ride ride = findByRideIdOrThrow(rideId);

		if (!CANCELLABLE.contains(ride.getStatus())) {
			throw new InvalidRideStateException(
					"Cannot cancel ride " + rideId + " from status " + ride.getStatus()
							+ ". Cancellation is only allowed from REQUESTED, ASSIGNED or ACCEPTED.");
		}

		Instant now = Instant.now();
		ride.setStatus(RideStatus.CANCELLED);
		ride.setCancelledAt(now);
		ride.setCancellationReason(request.getReason());
		ride.setUpdatedAt(now);

		return RideResponse.from(rideRepository.save(ride));
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private Ride findByRideIdOrThrow(String rideId) {
		return rideRepository.findByRideId(rideId)
				.orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));
	}

	private void assertStatus(Ride ride, RideStatus expected, String action) {
		if (ride.getStatus() != expected) {
			throw new InvalidRideStateException(
					"Cannot " + action + " ride " + ride.getRideId()
							+ ". Current status is " + ride.getStatus()
							+ "; expected " + expected + ".");
		}
	}

	private String generateRideId() {
		String candidate;
		do {
			candidate = "RIDE" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		} while (rideRepository.existsByRideId(candidate));
		return candidate;
	}
}
