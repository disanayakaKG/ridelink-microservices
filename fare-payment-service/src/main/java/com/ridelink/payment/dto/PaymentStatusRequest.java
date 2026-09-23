package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentStatusRequest {

    @NotNull
    private PaymentStatus status;
}
