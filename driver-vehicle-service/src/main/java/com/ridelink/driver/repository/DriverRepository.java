package com.ridelink.driver.repository;

import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// Spring Data MongoDB repository for performing database operations on Driver documents
public interface DriverRepository extends MongoRepository<Driver, String> {

    // Finds a driver profile associated with a specific user account ID
    Optional<Driver> findByAccountId(String accountId);

    // Retrieves all drivers currently matching the given status
    List<Driver> findByStatus(DriverStatus status);

    // Retrieves drivers matching a given status within a specific service area
    List<Driver> findByStatusAndServiceArea(DriverStatus status, String serviceArea);
}
