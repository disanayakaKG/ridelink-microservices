package com.ridelink.account.repository;

import com.ridelink.account.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Persistence abstraction for User documents. Spring Data MongoDB supplies the implementation,
 * keeping database-access concerns outside the business-service layer.
 */
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}