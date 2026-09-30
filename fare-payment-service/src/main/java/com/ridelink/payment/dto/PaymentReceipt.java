package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentStatus;
import java.time.LocalDateTime;

public record PaymentReceipt(String paymentId, String transactionReference,
        String rideId, String passengerId, Double amount, String paymentMethod,
        PaymentStatus status, LocalDateTime createdAt) {
}
