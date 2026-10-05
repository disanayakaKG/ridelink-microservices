package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareCalculationRequest;
import com.ridelink.payment.dto.FareCalculationResponse;

/**
 * Focused contract for decimal fare calculation.
 *
 * SOLID - Interface Segregation Principle: exposes calculation without payment persistence
 * responsibilities.
 */
public interface FareCalculationService {
    FareCalculationResponse calculateFare(FareCalculationRequest request);
}
