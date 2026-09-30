package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateRequest {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}