package com.ridelink.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Validated status-change payload limited to ACTIVE, SUSPENDED or DEACTIVATED; AccountService
 * enforces the administrator requirement.
 */
public class UpdateStatusRequest {

    @NotBlank
    @Pattern(regexp = "ACTIVE|SUSPENDED|DEACTIVATED")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}