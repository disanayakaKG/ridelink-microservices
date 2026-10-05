package com.ridelink.ride.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.ride.dto.AssignDriverRequest;
import com.ridelink.ride.dto.CancelRideRequest;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.service.RideService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// REST controller exposing HTTP endpoints for ride booking, queries, and lifecycle operations.
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Management", description = "Ride request, assignment and lifecycle APIs")
public class RideController {

	// Service layer handling ride business logic and integrations
	private final RideService rideService;

	// Creates a new ride request and calculates the initial fare estimate.
	@PostMapping
	@Operation(summary = "Create a new ride request")
	public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
		RideResponse response = rideService.createRide(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// Retrieves a ride record using its business ride identifier (e.g., RIDE001).
	@GetMapping("/{rideId}")
	@Operation(summary = "Get ride by business rideId")
	public ResponseEntity<RideResponse> getByRideId(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.getByRideId(rideId));
	}

	// Retrieves a ride record using its internal MongoDB document ID.
	@GetMapping("/id/{id}")
	@Operation(summary = "Get ride by MongoDB document id")
	public ResponseEntity<RideResponse> getByMongoId(@PathVariable String id) {
		return ResponseEntity.ok(rideService.getByMongoId(id));
	}

	// Retrieves all rides requested by a specific passenger.
	@GetMapping("/passenger/{passengerId}")
	@Operation(summary = "List rides for a passenger")
	public ResponseEntity<List<RideResponse>> getByPassenger(@PathVariable String passengerId) {
		return ResponseEntity.ok(rideService.getByPassengerId(passengerId));
	}

	// Retrieves all rides assigned to a specific driver.
	@GetMapping("/driver/{driverId}")
	@Operation(summary = "List rides for a driver")
	public ResponseEntity<List<RideResponse>> getByDriver(@PathVariable String driverId) {
		return ResponseEntity.ok(rideService.getByDriverId(driverId));
	}

	// Assigns an available driver to a ride (auto-selects first available driver if unspecified).
	@PostMapping("/{rideId}/assign")
	@Operation(summary = "Assign an available driver to a REQUESTED ride")
	public ResponseEntity<RideResponse> assignDriver(
			@PathVariable String rideId,
			@RequestBody(required = false) AssignDriverRequest request) {
		if (request == null) {
			request = new AssignDriverRequest();
		}
		return ResponseEntity.ok(rideService.assignDriver(rideId, request));
	}

	// Transitions ride state to ACCEPTED when the assigned driver accepts the ride.
	@PostMapping("/{rideId}/accept")
	@Operation(summary = "Driver accepts an ASSIGNED ride")
	public ResponseEntity<RideResponse> acceptRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.acceptRide(rideId));
	}

	// Transitions ride state to IN_PROGRESS when the trip commences.
	@PostMapping("/{rideId}/start")
	@Operation(summary = "Start an ACCEPTED ride (status → IN_PROGRESS)")
	public ResponseEntity<RideResponse> startRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.startRide(rideId));
	}

	// Completes an active ride, calculates final charges, and triggers payment creation.
	@PostMapping("/{rideId}/complete")
	@Operation(summary = "Complete an IN_PROGRESS ride, calculate final fare and create payment")
	public ResponseEntity<RideResponse> completeRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.completeRide(rideId));
	}

	// Cancels a ride if it is in REQUESTED, ASSIGNED, or ACCEPTED state.
	@PostMapping("/{rideId}/cancel")
	@Operation(summary = "Cancel a ride from REQUESTED, ASSIGNED or ACCEPTED")
	public ResponseEntity<RideResponse> cancelRide(
			@PathVariable String rideId,
			@Valid @RequestBody CancelRideRequest request) {
		return ResponseEntity.ok(rideService.cancelRide(rideId, request));
	}
}
