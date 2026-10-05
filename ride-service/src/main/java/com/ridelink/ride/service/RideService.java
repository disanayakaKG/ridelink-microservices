package com.ridelink.ride.service;

import java.util.List;

import com.ridelink.ride.dto.AssignDriverRequest;
import com.ridelink.ride.dto.CancelRideRequest;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;

/**
 * Business contract for ride creation, lookup, history and lifecycle operations.
 *
 * SOLID - Dependency Inversion Principle: controllers use this interface as the boundary to
 * ride business logic.
 */
public interface RideService {

	RideResponse createRide(CreateRideRequest request);

	RideResponse getByRideId(String rideId);

	RideResponse getByMongoId(String id);

	List<RideResponse> getByPassengerId(String passengerId);

	List<RideResponse> getByDriverId(String driverId);

	RideResponse assignDriver(String rideId, AssignDriverRequest request);

	RideResponse acceptRide(String rideId);

	RideResponse startRide(String rideId);

	RideResponse completeRide(String rideId);

	RideResponse cancelRide(String rideId, CancelRideRequest request);
}
