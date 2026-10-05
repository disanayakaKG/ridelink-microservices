package com.ridelink.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Payment creation payload requiring ride/passenger identifiers, a positive nonnull amount and
 * a nonblank method. The service stores the submitted amount rather than recalculating the
 * fare.
 */
@Data
public class PaymentRequest {
    @NotBlank
    private String rideId;
    @NotBlank
    private String passengerId;
    @NotNull
    @Positive
    private Double amount;
    @NotBlank
    private String paymentMethod;
}
