package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Profile registration payload requiring account linkage, license and vehicle details; service
 * area is optional. Duplicate account linkage is rejected by the service.
 */
@Data
public class DriverProfileRequest {

    @NotBlank
    private String accountId;

    @NotBlank
    private String licenseNumber;

    @NotBlank
    private String vehicleMake;

    @NotBlank
    private String vehicleModel;

    @NotBlank
    private String vehiclePlateNumber;

    private String serviceArea;
}