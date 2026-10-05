package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareRequest;
import com.ridelink.payment.dto.FareResponse;
import com.ridelink.payment.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Accepts validated fare-estimation requests and delegates calculation to FareService.
 *
 * SOLID - Single Responsibility Principle: HTTP concerns remain in this controller; business
 * operations are delegated to the service layer.
 *
 * SOLID - Dependency Inversion Principle: constructor injection supplies the service interface
 * rather than constructing an implementation.
 */
@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<FareResponse> estimateFare(
            @Valid @RequestBody FareRequest request) {

        return ResponseEntity.ok(
                fareService.calculateFare(request.getDistanceKm())
        );
    }
}
