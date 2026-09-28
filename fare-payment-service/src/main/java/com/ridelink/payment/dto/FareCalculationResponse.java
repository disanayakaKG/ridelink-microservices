package com.ridelink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareCalculationResponse {

    private BigDecimal distanceKm;
    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private BigDecimal totalFare;
}
