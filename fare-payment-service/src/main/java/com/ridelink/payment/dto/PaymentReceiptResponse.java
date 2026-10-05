package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Receipt projection of stored payment details, including transaction reference and current
 * status; receipt generation does not process a payment.
 */
@Data
@Builder
public class PaymentReceiptResponse {

    private String paymentId;
    private String transactionReference;
    private String rideId;
    private String passengerId;
    private Double amount;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
