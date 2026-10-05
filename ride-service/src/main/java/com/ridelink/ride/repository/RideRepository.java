package com.ridelink.ride.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;

// Spring Data MongoDB repository providing persistence and query methods for Ride documents.
public interface RideRepository extends MongoRepository<Ride, String> {

	// Custom finder and existence query methods for ride management
	Optional<Ride> findByRideId(String rideId);

	List<Ride> findByPassengerId(String passengerId);

	List<Ride> findByDriverId(String driverId);

	List<Ride> findByStatus(RideStatus status);

	boolean existsByRideId(String rideId);
}
