package com.ridelink.account.dto;

public class InternalAccountResponse {

    private String userId;
    private String role;
    private String status;
    private boolean active;

    public InternalAccountResponse() {}

    public InternalAccountResponse(String userId, String role, String status, boolean active) {
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.active = active;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}