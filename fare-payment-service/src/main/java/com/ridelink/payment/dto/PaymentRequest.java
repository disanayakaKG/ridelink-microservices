package com.ridelink.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
