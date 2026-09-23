package com.ridelink.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String rideId;

    private String passengerId;

    private Double amount;

    private String paymentMethod;

    private PaymentStatus status;

    private LocalDateTime createdAt;
}
