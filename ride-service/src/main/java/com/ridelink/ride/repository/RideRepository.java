package com.ridelink.ride.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;

/**
 * Persistence abstraction for Ride documents. Spring Data MongoDB supplies the implementation,
 * keeping database-access concerns outside the business-service layer.
 */
public interface RideRepository extends MongoRepository<Ride, String> {

	Optional<Ride> findByRideId(String rideId);

	List<Ride> findByPassengerId(String passengerId);

	List<Ride> findByDriverId(String driverId);

	List<Ride> findByStatus(RideStatus status);

	boolean existsByRideId(String rideId);
}
