package com.ridelink.payment.repository;

import com.ridelink.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

/**
 * Persistence abstraction for Payment documents. Spring Data MongoDB supplies the
 * implementation, keeping database-access concerns outside the business-service layer.
 */
public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRideId(String rideId);
    List<Payment> findByPassengerId(String passengerId);
}
