package com.ridelink.payment.service.impl;

import org.springframework.stereotype.Service;

import com.ridelink.payment.dto.FareResponse;
import com.ridelink.payment.service.FareService;

/**
 * Calculates fare = 150 + (distanceKm * 80).
 *
 * SOLID - Single Responsibility Principle: fare calculation is isolated from payment
 * persistence and status management.
 */
@Service
public class FareServiceImpl implements FareService {

    private static final double BASE_FARE = 150.0;
    private static final double RATE_PER_KM = 80.0;

    @Override
    public FareResponse calculateFare(Double distanceKm) {

        double estimatedFare =
                BASE_FARE + (distanceKm * RATE_PER_KM);

        return new FareResponse(
                distanceKm,
                BASE_FARE,
                RATE_PER_KM,
                estimatedFare
        );
    }
}
