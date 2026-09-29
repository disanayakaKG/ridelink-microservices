package com.ridelink.driver.repository;

import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends MongoRepository<Driver, String> {

    Optional<Driver> findByAccountId(String accountId);

    List<Driver> findByStatus(DriverStatus status);

    List<Driver> findByStatusAndServiceArea(DriverStatus status, String serviceArea);
}
