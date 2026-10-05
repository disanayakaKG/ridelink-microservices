package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Request payload for registering a new driver and vehicle profile
@Data
public class DriverProfileRequest {

    // Target user account ID and driver's license number
    @NotBlank
    private String accountId;

    @NotBlank
    private String licenseNumber;

    // Vehicle specifications and license plate details
    @NotBlank
    private String vehicleMake;

    @NotBlank
    private String vehicleModel;

    @NotBlank
    private String vehiclePlateNumber;

    // Primary operating city or geographical service area
    private String serviceArea;
}