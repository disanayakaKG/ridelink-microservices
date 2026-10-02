package com.ridelink.ride;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.mongodb.uri=mongodb://localhost:27017",
		"spring.mongodb.database=ridelink_ride_db_test",
		"app.driver-service.base-url=http://localhost:8082",
		"app.payment-service.base-url=http://localhost:8084"
})
class RideServiceApplicationTests extends com.ridelink.ride.security.AccountJwtTestSupport {

	@Test
	void contextLoads() {
	}
}
