package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Request payload used to update a driver's current GPS location
@Data
public class LocationUpdateRequest {

    // Mandatory latitude and longitude coordinates
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}