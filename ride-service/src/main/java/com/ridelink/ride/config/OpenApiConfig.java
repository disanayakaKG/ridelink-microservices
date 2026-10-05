package com.ridelink.ride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

// Configuration for OpenAPI 3.0 / Swagger documentation.
@Configuration
public class OpenApiConfig {

	// Defines general API metadata including title, description, version, and contact details.
	@Bean
	OpenAPI rideServiceOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("RideLink – Ride Management Service")
						.description("APIs for ride request creation, driver assignment and ride lifecycle management.")
						.version("1.0.0")
						.contact(new Contact()
								.name("RideLink Team")
								.email("ridelink@example.com")));
	}
}
