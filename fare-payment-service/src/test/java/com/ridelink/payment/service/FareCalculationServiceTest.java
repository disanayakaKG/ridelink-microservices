package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareCalculationRequest;
import com.ridelink.payment.dto.FareCalculationResponse;
import com.ridelink.payment.service.impl.FareCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FareCalculationServiceTest {

    private FareCalculationService fareCalculationService;

    @BeforeEach
    void setUp() {
        fareCalculationService = new FareCalculationServiceImpl();
    }

    @Test
    void testCalculateFare_Success() {
        FareCalculationRequest request = FareCalculationRequest.builder()
                .distanceKm(new BigDecimal("10"))
                .build();

        FareCalculationResponse response = fareCalculationService.calculateFare(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("10"), response.getDistanceKm());
        assertEquals(new BigDecimal("150"), response.getBaseFare());
        assertEquals(new BigDecimal("80"), response.getPerKmRate());
        assertEquals(new BigDecimal("950"), response.getTotalFare()); // 150 + (10 * 80) = 950
    }

    @Test
    void testCalculateFare_ZeroDistance_ThrowsException() {
        FareCalculationRequest request = FareCalculationRequest.builder()
                .distanceKm(BigDecimal.ZERO)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fareCalculationService.calculateFare(request)
        );

        assertEquals("Distance in KM must be greater than zero", exception.getMessage());
    }

    @Test
    void testCalculateFare_NegativeDistance_ThrowsException() {
        FareCalculationRequest request = FareCalculationRequest.builder()
                .distanceKm(new BigDecimal("-5"))
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fareCalculationService.calculateFare(request)
        );

        assertEquals("Distance in KM must be greater than zero", exception.getMessage());
    }
}
