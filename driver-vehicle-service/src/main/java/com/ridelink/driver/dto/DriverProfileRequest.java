package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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