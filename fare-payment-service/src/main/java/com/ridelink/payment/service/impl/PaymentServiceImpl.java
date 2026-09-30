package com.ridelink.payment.service.impl;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentReceipt;
import com.ridelink.payment.exception.PaymentNotFoundException;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.service.PaymentService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repository;

    public PaymentServiceImpl(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment createPayment(PaymentRequest request) {
        return repository.save(Payment.builder()
                .rideId(request.getRideId()).passengerId(request.getPassengerId())
                .amount(request.getAmount()).paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .transactionReference("TXN-" + UUID.randomUUID())
                .createdAt(LocalDateTime.now()).build());
    }

    @Override
    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    @Override
    public Payment getPaymentById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }

    @Override
    public Payment getPaymentByRideId(String rideId) {
        return repository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ride id: " + rideId));
    }

    @Override
    public List<Payment> getPassengerPayments(String passengerId) {
        return repository.findByPassengerId(passengerId);
    }

    @Override
    public Payment updateStatus(String id, PaymentStatus status) {
        Payment payment = getPaymentById(id);
        payment.setStatus(status);
        return repository.save(payment);
    }

    @Override
    public PaymentReceipt getReceipt(String id) {
        Payment payment = getPaymentById(id);
        return new PaymentReceipt(payment.getId(), payment.getTransactionReference(),
                payment.getRideId(), payment.getPassengerId(), payment.getAmount(),
                payment.getPaymentMethod(), payment.getStatus(), payment.getCreatedAt());
    }
}
