package com.ridelink.ride.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;

public interface RideRepository extends MongoRepository<Ride, String> {

	Optional<Ride> findByRideId(String rideId);

	List<Ride> findByPassengerId(String passengerId);

	List<Ride> findByDriverId(String driverId);

	List<Ride> findByStatus(RideStatus status);

	boolean existsByRideId(String rideId);
}
