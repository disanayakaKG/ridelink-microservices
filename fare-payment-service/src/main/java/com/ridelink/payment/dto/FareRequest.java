package com.ridelink.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fare-estimation payload requiring distance of at least 0.1 kilometres.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareRequest {

    @NotNull(message = "Distance is required")
    @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
    private Double distanceKm;
}
