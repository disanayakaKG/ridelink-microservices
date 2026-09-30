package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;

import java.util.List;

public interface PaymentService {

    Payment createPayment(PaymentRequest request);

    Payment getPayment(String id);

    Payment getPaymentByRideId(String rideId);

    Payment updatePaymentStatus(String id, PaymentStatus status);

    List<Payment> getAllPayments();

    List<Payment> getPaymentsByPassengerId(String passengerId);
}
