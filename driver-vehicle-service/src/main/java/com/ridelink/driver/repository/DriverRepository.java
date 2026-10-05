package com.ridelink.driver.repository;

import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence abstraction for Driver documents. Spring Data MongoDB supplies the
 * implementation, keeping database-access concerns outside the business-service layer.
 */
public interface DriverRepository extends MongoRepository<Driver, String> {

    Optional<Driver> findByAccountId(String accountId);

    List<Driver> findByStatus(DriverStatus status);

    List<Driver> findByStatusAndServiceArea(DriverStatus status, String serviceArea);
}
