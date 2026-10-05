package com.ridelink.ride.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ridelink.ride.exception.ExternalServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * Synchronous REST client for interacting with the Driver & Vehicle Service.
 * Fetches currently available drivers and verifies individual driver availability.
 */
@Slf4j
@Component
public class DriverServiceClient {

	// HTTP client configured with the Driver Service base URL
	private final RestClient restClient;

	// Initializes the RestClient using the driver-service base URL from application configuration.
	public DriverServiceClient(@Value("${app.driver-service.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.build();
	}

	/**
	 * Returns driver IDs that are currently AVAILABLE.
	 * If the remote service is unreachable or returns an unexpected shape,
	 * an empty list is returned so the caller can surface a NoAvailableDriverException.
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAvailableDriverIds() {
		try {
			// Call the Driver Service endpoint to fetch active drivers
			List<Map<String, Object>> body = restClient.get()
					.uri("/api/drivers/available")
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

			if (body == null || body.isEmpty()) {
				return Collections.emptyList();
			}

			// Extract driver IDs from response payloads (supports both driverId and id fields)
			return body.stream()
					.map(m -> {
						Object id = m.get("driverId");
						if (id == null) {
							id = m.get("id");
						}
						return id != null ? id.toString() : null;
					})
					.filter(id -> id != null && !id.isBlank())
					.toList();
		} catch (RestClientException ex) {
			// Wrap and rethrow external communication errors
			log.warn("Driver service call failed: {}", ex.getMessage());
			throw new ExternalServiceException(
					"Unable to retrieve available drivers from Driver Service: " + ex.getMessage(), ex);
		}
	}

	// Checks if a specific driver ID is currently present in the available drivers list.
	public boolean isDriverAvailable(String driverId) {
		List<String> available = getAvailableDriverIds();
		return available.contains(driverId);
	}
}
