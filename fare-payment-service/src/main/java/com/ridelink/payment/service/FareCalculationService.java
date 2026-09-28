package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareCalculationRequest;
import com.ridelink.payment.dto.FareCalculationResponse;

public interface FareCalculationService {
    FareCalculationResponse calculateFare(FareCalculationRequest request);
}
