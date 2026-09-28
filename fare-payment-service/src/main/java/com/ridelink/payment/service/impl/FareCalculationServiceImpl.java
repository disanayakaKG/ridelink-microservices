package com.ridelink.payment.service.impl;

import com.ridelink.payment.dto.FareCalculationRequest;
import com.ridelink.payment.dto.FareCalculationResponse;
import com.ridelink.payment.service.FareCalculationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FareCalculationServiceImpl implements FareCalculationService {

    public static final BigDecimal BASE_FARE = new BigDecimal("150");
    public static final BigDecimal PER_KM_RATE = new BigDecimal("80");

    @Override
    public FareCalculationResponse calculateFare(FareCalculationRequest request) {
        if (request == null || request.getDistanceKm() == null || request.getDistanceKm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Distance in KM must be greater than zero");
        }

        BigDecimal distance = request.getDistanceKm();
        BigDecimal totalFare = BASE_FARE.add(distance.multiply(PER_KM_RATE));

        return FareCalculationResponse.builder()
                .distanceKm(distance)
                .baseFare(BASE_FARE)
                .perKmRate(PER_KM_RATE)
                .totalFare(totalFare)
                .build();
    }
}
