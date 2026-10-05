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
 * Synchronous REST adapter for Driver & Vehicle Service available-driver lookup. Injected
 * RestClient configuration supplies JWT forwarding; transport failures become
 * ExternalServiceException.
 */
@Slf4j
@Component
public class DriverServiceClient {

	private final RestClient restClient;

	public DriverServiceClient(@Value("${app.driver-service.base-url}") String baseUrl, RestClient.Builder builder) {
		this.restClient = builder.clone()
				.baseUrl(baseUrl)
				.build();
	}

	/**
	 * Returns available driver IDs, accepting driverId or id response fields. Empty responses
	 * produce an empty list; REST failures raise ExternalServiceException.
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAvailableDriverIds() {
		try {
			List<Map<String, Object>> body = restClient.get()
					.uri("/api/drivers/available")
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

			if (body == null || body.isEmpty()) {
				return Collections.emptyList();
			}

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
			log.warn("Driver service call failed: {}", ex.getMessage());
			throw new ExternalServiceException(
					"Unable to retrieve available drivers from Driver Service: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Verifies a specific driver is available. Returns true if present in available list.
	 */
	public boolean isDriverAvailable(String driverId) {
		List<String> available = getAvailableDriverIds();
		return available.contains(driverId);
	}
}
