package com.ridelink.driver.service;

import com.ridelink.driver.dto.DriverProfileRequest;
import com.ridelink.driver.dto.LocationUpdateRequest;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateDriverProfileException;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public Driver registerProfile(DriverProfileRequest request) {
        driverRepository.findByAccountId(request.getAccountId())
                .ifPresent(existing -> {
                    throw new DuplicateDriverProfileException("Driver profile already exists for this account");
                });

        Driver driver = Driver.builder()
                .accountId(request.getAccountId())
                .licenseNumber(request.getLicenseNumber())
                .vehicleMake(request.getVehicleMake())
                .vehicleModel(request.getVehicleModel())
                .vehiclePlateNumber(request.getVehiclePlateNumber())
                .serviceArea(request.getServiceArea())
                .status(DriverStatus.UNAVAILABLE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return driverRepository.save(driver);
    }

    public Driver getByAccountId(String accountId) {
        return driverRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DriverNotFoundException("No driver profile found for account " + accountId));
    }

    public Driver getById(String id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + id));
    }

    public Driver updateAvailability(String driverId, DriverStatus newStatus) {
        Driver driver = getById(driverId);
        driver.setStatus(newStatus);
        driver.setUpdatedAt(Instant.now());
        return driverRepository.save(driver);
    }

    public Driver updateLocation(String driverId, LocationUpdateRequest request) {
        Driver driver = getById(driverId);
        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());
        driver.setUpdatedAt(Instant.now());
        return driverRepository.save(driver);
    }

    public List<Driver> getEligibleAvailableDrivers(String serviceArea) {
        if (serviceArea == null || serviceArea.isBlank()) {
            return driverRepository.findByStatus(DriverStatus.AVAILABLE);
        }
        return driverRepository.findByStatusAndServiceArea(DriverStatus.AVAILABLE, serviceArea);
    }
}