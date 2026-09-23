package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentStatusRequest;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Payment>> getPaymentsByPassengerId(@PathVariable String passengerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByPassengerId(passengerId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable String id, @Valid @RequestBody PaymentStatusRequest request) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, request.getStatus()));
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<Payment> getPaymentByRideId(@PathVariable String rideId) {
        return ResponseEntity.ok(paymentService.getPaymentByRideId(rideId));
    }
}
