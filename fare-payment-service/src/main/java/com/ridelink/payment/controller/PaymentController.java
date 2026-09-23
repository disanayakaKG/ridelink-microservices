package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentReceiptResponse;
import com.ridelink.payment.dto.PaymentStatusRequest;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(request));
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return service.getAllPayments();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable String id) {
        return service.getPaymentById(id);
    }

    @GetMapping("/ride/{rideId}")
    public Payment getPaymentByRideId(@PathVariable String rideId) {
        return service.getPaymentByRideId(rideId);
    }

    @GetMapping("/passenger/{passengerId}")
    public List<Payment> getPassengerPayments(@PathVariable String passengerId) {
        return service.getPassengerPayments(passengerId);
    }

    @PatchMapping("/{id}/status")
    public Payment updateStatus(@PathVariable String id,
            @Valid @RequestBody PaymentStatusRequest request) {
        return service.updateStatus(id, request.getStatus());
    }

    @GetMapping("/{id}/receipt")
    public PaymentReceiptResponse getReceipt(@PathVariable String id) {
        return service.getReceipt(id);
    }
}
