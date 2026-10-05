package com.ridelink.driver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB driver profile linking an accountId to license, vehicle, service-area, availability
 * and current-location information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    private String accountId;

    private String licenseNumber;
    private String vehicleMake;
    private String vehicleModel;
    private String vehiclePlateNumber;

    private DriverStatus status;

    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;

    private Instant createdAt;
    private Instant updatedAt;
}
