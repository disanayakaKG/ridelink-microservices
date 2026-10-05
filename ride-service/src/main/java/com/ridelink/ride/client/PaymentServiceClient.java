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
 * Synchronous REST adapter for fare estimation and simulated payment creation. Injected
 * RestClient configuration supplies JWT forwarding; fare transport failures use the local 150 +
 * (distanceKm * 80) fallback.
 */
@Slf4j
@Component
public class PaymentServiceClient {

	private final RestClient restClient;

	public PaymentServiceClient(@Value("${app.payment-service.base-url}") String baseUrl, RestClient.Builder builder) {
		this.restClient = builder.clone()
				.baseUrl(baseUrl)
				.build();
	}

	/**
	 * Requests a fare estimate synchronously; REST transport failures fall back to 150 +
	 * (distanceKm * 80), while a missing estimate field raises ExternalServiceException.
	 */
	public double estimateFare(double distanceKm) {
		try {
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
			log.warn("Payment service fare estimate failed: {}", ex.getMessage());
			// Fallback to documented rule so ride flow can continue offline during local demo
			double fallback = 150.0 + (distanceKm * 80.0);
			log.info("Using local fare fallback: {}", fallback);
			return fallback;
		}
	}

	/**
	 * Creates a simulated CARD payment. Missing response identifiers return null; REST failures
	 * raise ExternalServiceException.
	 */
	@SuppressWarnings("unchecked")
	public String createPayment(String rideId, String passengerId, double amount) {
		try {
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
			Object id = response.get("id");
			if (id == null) {
				id = response.get("paymentId");
			}
			return id != null ? id.toString() : null;
		} catch (RestClientException ex) {
			log.warn("Payment service create payment failed: {}", ex.getMessage());
			throw new ExternalServiceException(
					"Unable to create payment via Payment Service: " + ex.getMessage(), ex);
		}
	}
}
