package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareResponse;

public interface FareService {

    FareResponse calculateFare(Double distanceKm);
}
