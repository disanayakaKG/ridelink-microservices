package com.ridelink.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Decimal fare-calculation payload requiring distance of at least 0.01 kilometres.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareCalculationRequest {

    @NotNull(message = "Distance in KM is required")
    @DecimalMin(value = "0.01", message = "Distance must be greater than zero")
    private BigDecimal distanceKm;
}
