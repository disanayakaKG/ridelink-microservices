package com.ridelink.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

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
