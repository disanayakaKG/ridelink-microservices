package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareCalculationRequest;
import com.ridelink.payment.dto.FareCalculationResponse;
import com.ridelink.payment.service.FareCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/fare")
public class FareCalculationController {

    private final FareCalculationService fareCalculationService;

    // Constructor injection
    public FareCalculationController(FareCalculationService fareCalculationService) {
        this.fareCalculationService = fareCalculationService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<FareCalculationResponse> calculateFare(@Valid @RequestBody FareCalculationRequest request) {
        FareCalculationResponse response = fareCalculationService.calculateFare(request);
        return ResponseEntity.ok(response);
    }
}
