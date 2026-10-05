package com.ridelink.ride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// Configuration providing shared REST client builder beans for inter-service HTTP communication.
@Configuration
public class RestClientConfig {

	// Provides a customizable RestClient.Builder bean across the application context.
	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}
}
