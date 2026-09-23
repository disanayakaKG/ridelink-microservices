package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareCalculationRequest;
import com.ridelink.payment.dto.FareCalculationResponse;
import com.ridelink.payment.service.FareCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FareCalculationControllerTest {

    private FareCalculationService fareCalculationService;
    private FareCalculationController fareCalculationController;

    @BeforeEach
    void setUp() {
        fareCalculationService = Mockito.mock(FareCalculationService.class);
        fareCalculationController = new FareCalculationController(fareCalculationService);
    }

    @Test
    void calculateFare_ReturnsOkResponse() {
        FareCalculationRequest request = FareCalculationRequest.builder()
                .distanceKm(new BigDecimal("10"))
                .build();

        FareCalculationResponse mockResponse = FareCalculationResponse.builder()
                .distanceKm(new BigDecimal("10"))
                .baseFare(new BigDecimal("150"))
                .perKmRate(new BigDecimal("80"))
                .totalFare(new BigDecimal("950"))
                .build();

        when(fareCalculationService.calculateFare(any(FareCalculationRequest.class))).thenReturn(mockResponse);

        ResponseEntity<FareCalculationResponse> response = fareCalculationController.calculateFare(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(new BigDecimal("950"), response.getBody().getTotalFare());
    }
}
