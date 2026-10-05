package com.ridelink.ride.service;

import java.util.List;

import com.ridelink.ride.dto.AssignDriverRequest;
import com.ridelink.ride.dto.CancelRideRequest;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;

// Service contract defining operations for booking, managing, and tracking rides.
public interface RideService {

	// Ride booking and retrieval operations
	RideResponse createRide(CreateRideRequest request);

	RideResponse getByRideId(String rideId);

	RideResponse getByMongoId(String id);

	List<RideResponse> getByPassengerId(String passengerId);

	List<RideResponse> getByDriverId(String driverId);

	// Ride lifecycle state transition operations
	RideResponse assignDriver(String rideId, AssignDriverRequest request);

	RideResponse acceptRide(String rideId);

	RideResponse startRide(String rideId);

	RideResponse completeRide(String rideId);

	RideResponse cancelRide(String rideId, CancelRideRequest request);
}
