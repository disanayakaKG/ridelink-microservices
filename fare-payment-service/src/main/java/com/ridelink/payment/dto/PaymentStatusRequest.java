package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Status update payload requiring a PaymentStatus value; this DTO does not define lifecycle
 * transition restrictions.
 */
@Data
public class PaymentStatusRequest {
    @NotNull
    private PaymentStatus status;
}
