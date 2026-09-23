package com.ridelink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FareResponse {

    private Double distanceKm;
    private Double baseFare;
    private Double perKmRate;
    private Double estimatedFare;
}
