package com.ridelink.account.dto;

/**
 * Partial profile update payload; the service ignores blank names and only updates a phone
 * value when supplied.
 */
public class UpdateProfileRequest {

    private String fullName;
    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}