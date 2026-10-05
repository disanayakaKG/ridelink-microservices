package com.ridelink.payment.service.impl;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentReceiptResponse;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.service.PaymentService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Persists simulated payments and builds receipts from stored records.
 *
 * SOLID - Dependency Inversion Principle: the injected PaymentRepository abstraction supplies
 * persistence operations.
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repository;

    public PaymentServiceImpl(PaymentRepository repository) {
        this.repository = repository;
    }

    /**
     * Stores the submitted payment as PENDING with a generated TXN-prefixed UUID reference; no
     * external payment gateway is invoked.
     */
    @Override
    public Payment createPayment(PaymentRequest request) {
        return repository.save(Payment.builder()
                .rideId(request.getRideId()).passengerId(request.getPassengerId())
                .amount(request.getAmount()).paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .transactionReference("TXN-" + UUID.randomUUID())
                .createdAt(LocalDateTime.now()).build());
    }

    /**
     * Returns all stored payment records.
     */
    @Override
    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    /**
     * Finds a payment by document identifier or reports it missing.
     */
    @Override
    public Payment getPaymentById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }

    /**
     * Finds a payment linked to the supplied ride or reports it missing.
     */
    @Override
    public Payment getPaymentByRideId(String rideId) {
        return repository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ride id: " + rideId));
    }

    /**
     * Returns stored payment history for the supplied passenger.
     */
    @Override
    public List<Payment> getPassengerPayments(String passengerId) {
        return repository.findByPassengerId(passengerId);
    }

    /**
     * Persists the supplied status without enforcing a transition graph.
     */
    @Override
    public Payment updateStatus(String id, PaymentStatus status) {
        Payment payment = getPaymentById(id);
        payment.setStatus(status);
        return repository.save(payment);
    }

    /**
     * Builds a receipt from the stored payment, including its current status and transaction
     * reference.
     */
    @Override
    public PaymentReceiptResponse getReceipt(String id) {
        Payment payment = getPaymentById(id);
        return PaymentReceiptResponse.builder()
                .paymentId(payment.getId())
                .transactionReference(payment.getTransactionReference())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
