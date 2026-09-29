package com.ridelink.driver.controller;

import com.ridelink.driver.dto.DriverProfileRequest;
import com.ridelink.driver.dto.LocationUpdateRequest;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<Driver> registerProfile(@Valid @RequestBody DriverProfileRequest request) {
        Driver driver = driverService.registerProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(driver);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getById(@PathVariable String id) {
        return ResponseEntity.ok(driverService.getById(id));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Driver> getByAccountId(@PathVariable String accountId) {
        return ResponseEntity.ok(driverService.getByAccountId(accountId));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<Driver> updateAvailability(@PathVariable String id,
                                                       @RequestParam DriverStatus status) {
        return ResponseEntity.ok(driverService.updateAvailability(id, status));
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<Driver> updateLocation(@PathVariable String id,
                                                   @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(id, request));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getEligibleAvailableDrivers(
            @RequestParam(required = false) String serviceArea) {
        return ResponseEntity.ok(driverService.getEligibleAvailableDrivers(serviceArea));
    }
}
