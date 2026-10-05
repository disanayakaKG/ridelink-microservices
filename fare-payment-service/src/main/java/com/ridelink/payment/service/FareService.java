package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareResponse;

/**
 * Focused contract for fare estimation using distance in kilometres.
 *
 * SOLID - Interface Segregation Principle: fare consumers do not depend on payment persistence
 * or status operations.
 */
public interface FareService {

    FareResponse calculateFare(Double distanceKm);
}
