package com.ridelink.driver.controller;

import com.ridelink.driver.dto.DriverProfileRequest;
import com.ridelink.driver.dto.LocationUpdateRequest;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller exposing HTTP endpoints for driver management, status updates, and location tracking
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Driver & Vehicle", description = "Driver profile, availability, location and eligible-driver lookup")
public class DriverController {

    // Driver business logic service dependency
    private final DriverService driverService;

    // Registers a new driver and vehicle profile linked to an existing user account
    @Operation(summary = "Register a driver profile",
            description = "Creates a driver profile linked to an existing account. Fails if a profile already exists for the account.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Driver profile created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Driver profile already exists for this account")
    })
    @PostMapping
    public ResponseEntity<Driver> registerProfile(@Valid @RequestBody DriverProfileRequest request) {
        Driver driver = driverService.registerProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(driver);
    }

    // Retrieves a single driver profile using its internal database ID
    @Operation(summary = "Get driver by ID", description = "Retrieves a single driver profile by its internal ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver found"),
            @ApiResponse(responseCode = "404", description = "No driver exists with this ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Driver> getById(@Parameter(description = "Driver's internal ID") @PathVariable String id) {
        return ResponseEntity.ok(driverService.getById(id));
    }

    // Retrieves a driver profile using the linked account ID from the auth service
    @Operation(summary = "Get driver by account ID", description = "Retrieves a driver profile using the linked Account Service account ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver found"),
            @ApiResponse(responseCode = "404", description = "No driver profile linked to this account")
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Driver> getByAccountId(@Parameter(description = "Account Service account ID") @PathVariable String accountId) {
        return ResponseEntity.ok(driverService.getByAccountId(accountId));
    }

    // Updates a driver's operational status (AVAILABLE, UNAVAILABLE, or ON_TRIP)
    @Operation(summary = "Update driver availability",
            description = "Sets a driver's status to AVAILABLE, UNAVAILABLE, or ON_TRIP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability updated"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Driver> updateAvailability(
            @Parameter(description = "Driver's internal ID") @PathVariable String id,
            @Parameter(description = "New status: AVAILABLE, UNAVAILABLE, or ON_TRIP") @RequestParam DriverStatus status) {
        return ResponseEntity.ok(driverService.updateAvailability(id, status));
    }

    // Updates a driver's current geographic coordinates
    @Operation(summary = "Update driver's simulated location",
            description = "Sets the driver's current latitude and longitude.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated"),
            @ApiResponse(responseCode = "400", description = "Invalid latitude/longitude"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PatchMapping("/{id}/location")
    public ResponseEntity<Driver> updateLocation(
            @Parameter(description = "Driver's internal ID") @PathVariable String id,
            @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(id, request));
    }

    // Retrieves available drivers for ride matching, optionally filtered by service region
    @Operation(summary = "List eligible available drivers",
            description = "Returns drivers currently AVAILABLE, optionally filtered by service area. Used by the Ride Service to find a driver for assignment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of eligible drivers (may be empty if none available)")
    })
    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getEligibleAvailableDrivers(
            @Parameter(description = "Optional service area filter, e.g. Colombo") @RequestParam(required = false) String serviceArea) {
        return ResponseEntity.ok(driverService.getEligibleAvailableDrivers(serviceArea));
    }
}