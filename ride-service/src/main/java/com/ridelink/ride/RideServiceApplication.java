spring.application.name=ride-service
server.port=8083

# MongoDB
spring.mongodb.uri=${MONGODB_URI}
spring.mongodb.database=ridelink_ride_db

# Inter-service base URLs
app.driver-service.base-url=${DRIVER_SERVICE_URL:http://localhost:8082}
app.payment-service.base-url=${PAYMENT_SERVICE_URL:http://localhost:8084}

# OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method

# Logging
logging.level.com.ridelink.ride=INFO
logging.level.org.springframework.data.mongodb=WARN
