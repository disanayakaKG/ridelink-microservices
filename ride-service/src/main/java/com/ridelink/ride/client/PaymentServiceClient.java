package com.ridelink.ride.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ridelink.ride.exception.ExternalServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * Synchronous REST client for the Fare & Payment Service.
 * Used for estimating ride fares and creating payment records upon ride completion.
 */
@Slf4j
@Component
public class PaymentServiceClient {

	// HTTP client configured with the Payment Service base URL
	private final RestClient restClient;

	// Initializes the RestClient using the payment-service base URL from application configuration.
	public PaymentServiceClient(@Value("${app.payment-service.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
	}

	// Requests fare estimation based on trip distance, with an offline calculation fallback.
	public double estimateFare(double distanceKm) {
		try {
			// Request fare estimation from the remote Payment Service
			Map<String, Object> request = Map.of("distanceKm", distanceKm);
			Map<?, ?> response = restClient.post()
					.uri("/api/fares/estimate")
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(Map.class);

			if (response == null || response.get("estimatedFare") == null) {
				throw new ExternalServiceException("Fare estimate response missing estimatedFare");
			}
			return ((Number) response.get("estimatedFare")).doubleValue();
		} catch (RestClientException ex) {
			// Fall back to standard local formula (Base 150 + 80/km) if service is unreachable
			log.warn("Payment service fare estimate failed: {}", ex.getMessage());
			double fallback = 150.0 + (distanceKm * 80.0);
			log.info("Using local fare fallback: {}", fallback);
			return fallback;
		}
	}

	// Creates a payment transaction for a completed ride and returns the generated payment ID.
	@SuppressWarnings("unchecked")
	public String createPayment(String rideId, String passengerId, double amount) {
		try {
			// Post payment transaction details to Payment Service
			Map<String, Object> request = Map.of(
					"rideId", rideId,
					"passengerId", passengerId,
					"amount", amount,
					"paymentMethod", "CARD");

			Map<String, Object> response = restClient.post()
					.uri("/api/payments")
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(Map.class);

			if (response == null) {
				return null;
			}

			// Extract payment ID from either id or paymentId response field
			Object id = response.get("id");
			if (id == null) {
				id = response.get("paymentId");
			}
			return id != null ? id.toString() : null;
		} catch (RestClientException ex) {
			// Wrap and report remote payment processing failures
			log.warn("Payment service create payment failed: {}", ex.getMessage());
			throw new ExternalServiceException(
					"Unable to create payment via Payment Service: " + ex.getMessage(), ex);
		}
	}
}
