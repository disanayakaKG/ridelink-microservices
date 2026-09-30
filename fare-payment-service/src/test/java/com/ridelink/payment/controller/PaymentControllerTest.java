package com.ridelink.payment.controller;

import com.ridelink.payment.exception.GlobalExceptionHandler;
import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import com.ridelink.payment.repository.PaymentRepository;
import com.ridelink.payment.service.impl.FareServiceImpl;
import com.ridelink.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentControllerTest {

    private PaymentRepository repository;
    private MockMvc mockMvc;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        repository = mock(PaymentRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PaymentController(new PaymentServiceImpl(repository)),
                        new FareController(new FareServiceImpl()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        savedPayment = Payment.builder()
                .id("payment-123").rideId("RIDE001").passengerId("PASS001")
                .amount(950.0).paymentMethod("CARD").status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.of(2026, 9, 23, 21, 30)).build();
    }

    @Test
    void createPaymentSavesPendingPaymentAndReturnsCreated() throws Exception {
        when(repository.save(any(Payment.class))).thenReturn(savedPayment);
        LocalDateTime before = LocalDateTime.now();

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rideId":"RIDE001","passengerId":"PASS001",
                                 "amount":950.0,"paymentMethod":"CARD"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("payment-123"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(950.0))
                .andExpect(jsonPath("$.createdAt").exists());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(captor.capture());
        Payment payment = captor.getValue();
        assertNull(payment.getId());
        assertEquals("RIDE001", payment.getRideId());
        assertEquals("PASS001", payment.getPassengerId());
        assertEquals(950.0, payment.getAmount());
        assertEquals("CARD", payment.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertFalse(payment.getCreatedAt().isBefore(before));
        assertFalse(payment.getCreatedAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void createPaymentsGeneratesDistinctTransactionReferences() throws Exception {
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"rideId":"RIDE002","passengerId":"PASS001",
                                     "amount":1200.0,"paymentMethod":"CARD"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transactionReference", startsWith("TXN-")));
        }

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(repository, times(2)).save(captor.capture());
        List<Payment> payments = captor.getAllValues();
        for (Payment payment : payments) {
            String reference = payment.getTransactionReference();
            assertTrue(reference.startsWith("TXN-"));
            assertEquals(reference.substring(4), UUID.fromString(reference.substring(4)).toString());
        }
        assertNotEquals(payments.get(0).getTransactionReference(), payments.get(1).getTransactionReference());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "TXN-550e8400-e29b-41d4-a716-446655440000")
    void statusUpdatePreservesTransactionReferenceIncludingLegacyNull(String reference) throws Exception {
        savedPayment.setTransactionReference(reference);
        when(repository.findById("payment-123")).thenReturn(Optional.of(savedPayment));
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/payments/payment-123/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUCCESS\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(captor.capture());
        assertEquals(reference, captor.getValue().getTransactionReference());
        assertEquals(PaymentStatus.SUCCESS, captor.getValue().getStatus());
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void getReceiptReturnsExistingPaymentWithoutSaving(PaymentStatus paymentStatus) throws Exception {
        String reference = "TXN-550e8400-e29b-41d4-a716-446655440000";
        savedPayment.setTransactionReference(reference);
        savedPayment.setStatus(paymentStatus);
        when(repository.findById("payment-123")).thenReturn(Optional.of(savedPayment));

        mockMvc.perform(get("/api/payments/payment-123/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("payment-123"))
                .andExpect(jsonPath("$.transactionReference").value(reference))
                .andExpect(jsonPath("$.rideId").value("RIDE001"))
                .andExpect(jsonPath("$.passengerId").value("PASS001"))
                .andExpect(jsonPath("$.amount").value(950.0))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.status").value(paymentStatus.name()))
                .andExpect(jsonPath("$.createdAt").value("2026-09-23T21:30:00"));
        verify(repository).findById("payment-123");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getLegacyReceiptAllowsNullTransactionReference() throws Exception {
        when(repository.findById("payment-123")).thenReturn(Optional.of(savedPayment));

        mockMvc.perform(get("/api/payments/payment-123/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("payment-123"))
                .andExpect(jsonPath("$.transactionReference").value(nullValue()));
        verify(repository).findById("payment-123");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getReceiptForUnknownPaymentReturnsNotFound() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/missing/receipt"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found with id: missing"));
        verify(repository).findById("missing");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllPaymentsReturnsPaymentsAcrossPassengers() throws Exception {
        Payment anotherPayment = Payment.builder()
                .id("payment-456").rideId("RIDE002").passengerId("PASS002")
                .amount(500.0).paymentMethod("CASH").status(PaymentStatus.SUCCESS)
                .createdAt(savedPayment.getCreatedAt()).build();
        when(repository.findAll()).thenReturn(List.of(savedPayment, anotherPayment));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("payment-123"))
                .andExpect(jsonPath("$[0].passengerId").value("PASS001"))
                .andExpect(jsonPath("$[1].id").value("payment-456"))
                .andExpect(jsonPath("$[1].passengerId").value("PASS002"));
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPassengerPaymentHistoryReturnsMatchingPayments() throws Exception {
        Payment anotherPayment = Payment.builder()
                .id("payment-456").rideId("RIDE002").passengerId("PASS001")
                .amount(500.0).paymentMethod("CASH").status(PaymentStatus.SUCCESS)
                .createdAt(savedPayment.getCreatedAt()).build();
        when(repository.findByPassengerId("PASS001")).thenReturn(List.of(savedPayment, anotherPayment));

        mockMvc.perform(get("/api/payments/passenger/PASS001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("payment-123"))
                .andExpect(jsonPath("$[0].rideId").value("RIDE001"))
                .andExpect(jsonPath("$[0].passengerId").value("PASS001"))
                .andExpect(jsonPath("$[0].amount").value(950.0))
                .andExpect(jsonPath("$[0].paymentMethod").value("CARD"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[1].id").value("payment-456"))
                .andExpect(jsonPath("$[1].passengerId").value("PASS001"));
        verify(repository).findByPassengerId("PASS001");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllPaymentsReturnsEmptyArrayWhenNoPaymentsExist() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPassengerPaymentHistoryReturnsEmptyArrayWhenNoPaymentsExist() throws Exception {
        when(repository.findByPassengerId("PASS_UNKNOWN")).thenReturn(List.of());

        mockMvc.perform(get("/api/payments/passenger/PASS_UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(repository).findByPassengerId("PASS_UNKNOWN");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPaymentByIdReturnsSavedPayment() throws Exception {
        when(repository.findById("payment-123")).thenReturn(Optional.of(savedPayment));
        mockMvc.perform(get("/api/payments/payment-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("payment-123"));
        verify(repository).findById("payment-123");
    }

    @Test
    void getPaymentByRideIdReturnsSavedPayment() throws Exception {
        when(repository.findByRideId("RIDE001")).thenReturn(Optional.of(savedPayment));
        mockMvc.perform(get("/api/payments/ride/RIDE001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value("RIDE001"));
        verify(repository).findByRideId("RIDE001");
    }

    @Test
    void missingPaymentIdReturnsNotFound() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/payments/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found with id: missing"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void missingRideIdReturnsNotFound() throws Exception {
        when(repository.findByRideId("missing")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/payments/ride/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found with ride id: missing"));
    }

    @Test
    void invalidPaymentReturnsBadRequestWithoutSaving() throws Exception {
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rideId":"","passengerId":"","amount":-100,"paymentMethod":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("rideId: must not be blank")))
                .andExpect(jsonPath("$.details", hasItem("passengerId: must not be blank")))
                .andExpect(jsonPath("$.details", hasItem("amount: must be greater than 0")))
                .andExpect(jsonPath("$.details", hasItem("paymentMethod: must not be blank")));
        verifyNoInteractions(repository);
    }

    @Test
    void missingAmountReturnsBadRequestWithoutSaving() throws Exception {
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rideId":"RIDE001","passengerId":"PASS001","paymentMethod":"CARD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("amount: must not be null")));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void updateStatusSavesExistingPaymentAndPreservesOtherFields(PaymentStatus newStatus) throws Exception {
        LocalDateTime originalCreatedAt = savedPayment.getCreatedAt();
        when(repository.findById("payment-123")).thenReturn(Optional.of(savedPayment));
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/payments/payment-123/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + newStatus.name() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("payment-123"))
                .andExpect(jsonPath("$.rideId").value("RIDE001"))
                .andExpect(jsonPath("$.passengerId").value("PASS001"))
                .andExpect(jsonPath("$.amount").value(950.0))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.status").value(newStatus.name()))
                .andExpect(jsonPath("$.createdAt").exists());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        var order = inOrder(repository);
        order.verify(repository).findById("payment-123");
        order.verify(repository).save(captor.capture());
        assertSame(savedPayment, captor.getValue());
        assertEquals(newStatus, captor.getValue().getStatus());
        assertEquals(originalCreatedAt, captor.getValue().getCreatedAt());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void updateStatusForMissingPaymentReturnsNotFoundWithoutSaving() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/payments/missing/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUCCESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Payment not found with id: missing"));

        verify(repository).findById("missing");
        verify(repository, never()).save(any(Payment.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"status\":null}"})
    void missingOrNullStatusReturnsBadRequest(String body) throws Exception {
        mockMvc.perform(patch("/api/payments/payment-123/status").contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("status: must not be null")));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"status\":\"REFUNDED\"}", "{\"status\":\"success\"}",
            "{\"status\":{}}", "{", "", "null"})
    void invalidStatusOrBodyReturnsBadRequest(String body) throws Exception {
        mockMvc.perform(patch("/api/payments/payment-123/status").contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(repository);
    }

    @Test
    void fareEstimateAndValidationRemainUnchanged() throws Exception {
        mockMvc.perform(post("/api/fares/estimate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanceKm\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedFare").value(950.0));
        mockMvc.perform(post("/api/fares/estimate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanceKm\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem("distanceKm: Distance must be greater than 0")));
    }
}
