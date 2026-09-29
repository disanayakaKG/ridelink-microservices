package com.ridelink.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateStatusRequest {

    @NotBlank
    @Pattern(regexp = "ACTIVE|SUSPENDED|DEACTIVATED")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}