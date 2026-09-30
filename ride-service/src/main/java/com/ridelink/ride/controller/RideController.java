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

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Management", description = "Ride request, assignment and lifecycle APIs")
public class RideController {

	private final RideService rideService;

	@PostMapping
	@Operation(summary = "Create a new ride request")
	public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
		RideResponse response = rideService.createRide(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{rideId}")
	@Operation(summary = "Get ride by business rideId")
	public ResponseEntity<RideResponse> getByRideId(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.getByRideId(rideId));
	}

	@GetMapping("/id/{id}")
	@Operation(summary = "Get ride by MongoDB document id")
	public ResponseEntity<RideResponse> getByMongoId(@PathVariable String id) {
		return ResponseEntity.ok(rideService.getByMongoId(id));
	}

	@GetMapping("/passenger/{passengerId}")
	@Operation(summary = "List rides for a passenger")
	public ResponseEntity<List<RideResponse>> getByPassenger(@PathVariable String passengerId) {
		return ResponseEntity.ok(rideService.getByPassengerId(passengerId));
	}

	@GetMapping("/driver/{driverId}")
	@Operation(summary = "List rides for a driver")
	public ResponseEntity<List<RideResponse>> getByDriver(@PathVariable String driverId) {
		return ResponseEntity.ok(rideService.getByDriverId(driverId));
	}

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

	@PostMapping("/{rideId}/accept")
	@Operation(summary = "Driver accepts an ASSIGNED ride")
	public ResponseEntity<RideResponse> acceptRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.acceptRide(rideId));
	}

	@PostMapping("/{rideId}/start")
	@Operation(summary = "Start an ACCEPTED ride (status → IN_PROGRESS)")
	public ResponseEntity<RideResponse> startRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.startRide(rideId));
	}

	@PostMapping("/{rideId}/complete")
	@Operation(summary = "Complete an IN_PROGRESS ride, calculate final fare and create payment")
	public ResponseEntity<RideResponse> completeRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.completeRide(rideId));
	}

	@PostMapping("/{rideId}/cancel")
	@Operation(summary = "Cancel a ride from REQUESTED, ASSIGNED or ACCEPTED")
	public ResponseEntity<RideResponse> cancelRide(
			@PathVariable String rideId,
			@Valid @RequestBody CancelRideRequest request) {
		return ResponseEntity.ok(rideService.cancelRide(rideId, request));
	}
}
