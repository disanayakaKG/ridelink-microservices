package com.ridelink.ride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main entry point for the Ride Management microservice.
@SpringBootApplication
public class RideServiceApplication {

	// Boots up the Spring application context and embedded web server.
	public static void main(String[] args) {
		SpringApplication.run(RideServiceApplication.class, args);
	}
}
