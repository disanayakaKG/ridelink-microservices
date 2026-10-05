package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Location update payload requiring both latitude and longitude; the annotations do not impose
 * geographic range limits.
 */
@Data
public class LocationUpdateRequest {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}