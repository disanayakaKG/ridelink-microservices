package com.ridelink.driver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// MongoDB entity representing a registered driver, vehicle details, and current status
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "drivers")
public class Driver {

    // Database identifier and associated user account identifier
    @Id
    private String id;
    private String accountId;

    // Driver license and vehicle specifications
    private String licenseNumber;
    private String vehicleMake;
    private String vehicleModel;
    private String vehiclePlateNumber;

    // Current availability status (AVAILABLE, UNAVAILABLE, ON_TRIP)
    private DriverStatus status;

    // Operating service area and real-time geographic location coordinates
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;

    // Record creation and last update timestamps
    private Instant createdAt;
    private Instant updatedAt;
}
