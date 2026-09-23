package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentReceiptResponse;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import java.util.List;

public interface PaymentService {
    Payment createPayment(PaymentRequest request);
    List<Payment> getAllPayments();
    Payment getPaymentById(String id);
    Payment getPaymentByRideId(String rideId);
    List<Payment> getPassengerPayments(String passengerId);
    Payment updateStatus(String id, PaymentStatus status);
    PaymentReceiptResponse getReceipt(String id);
}
