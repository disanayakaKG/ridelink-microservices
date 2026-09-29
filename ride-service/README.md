# Ride Management Service

**Port:** `8083`  
**Database:** `ridelink_ride_db`  
**Package:** `com.ridelink.ride`

Primary owner: **Member 3** · Branch: `feature/ride-service`

---

## Responsibility

- Create ride requests (pickup, destination, distance)
- Obtain eligible available drivers from Driver & Vehicle Service
- Assign a driver (auto-select first available, or explicit `driverId`)
- Manage ride lifecycle with strict state transitions
- Calculate final fare and create simulated payment via Fare & Payment Service
- Cancel rides from allowed states
- Retrieve rides by rideId, passenger, or driver

---

## Ride lifecycle

```text
REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED
    │           │          │
    └───────────┴──────────┴──→ CANCELLED
```

| Action        | From status   | To status     |
|---------------|---------------|---------------|
| create        | —             | REQUESTED     |
| assign        | REQUESTED     | ASSIGNED      |
| accept        | ASSIGNED      | ACCEPTED      |
| start         | ACCEPTED      | IN_PROGRESS   |
| complete      | IN_PROGRESS   | COMPLETED     |
| cancel        | REQUESTED / ASSIGNED / ACCEPTED | CANCELLED |

Invalid transitions return **409 Conflict**.

---

## API endpoints

| Method | Path                              | Description                          |
|--------|-----------------------------------|--------------------------------------|
| POST   | `/api/rides`                      | Create ride request                  |
| GET    | `/api/rides/{rideId}`             | Get by business rideId               |
| GET    | `/api/rides/id/{id}`              | Get by MongoDB id                    |
| GET    | `/api/rides/passenger/{id}`       | List by passenger                    |
| GET    | `/api/rides/driver/{id}`          | List by driver                       |
| POST   | `/api/rides/{rideId}/assign`      | Assign available driver              |
| POST   | `/api/rides/{rideId}/accept`      | Driver accepts                       |
| POST   | `/api/rides/{rideId}/start`       | Start ride                           |
| POST   | `/api/rides/{rideId}/complete`    | Complete + final fare + payment      |
| POST   | `/api/rides/{rideId}/cancel`      | Cancel with reason                   |

Swagger UI: http://localhost:8083/swagger-ui.html

---

## Inter-service communication (synchronous REST)

1. **Driver & Vehicle Service** (`http://localhost:8082`)  
   - `GET /api/drivers/available` → list of available drivers  
   - Used during assign to select or validate a driver.

2. **Fare & Payment Service** (`http://localhost:8084`)  
   - `POST /api/fares/estimate` `{ distanceKm }` → estimated fare  
   - `POST /api/payments` → create simulated payment on complete  
   - Local fallback fare rule if payment service is down:  
     `Base 150 + (distanceKm × 80)`

Stable identifiers used: `rideId`, `passengerId`, `driverId`, `paymentId`.

---

## Configuration

```properties
spring.application.name=ride-service
server.port=8083
spring.mongodb.uri=${MONGODB_URI}
spring.mongodb.database=ridelink_ride_db
app.driver-service.base-url=${DRIVER_SERVICE_URL:http://localhost:8082}
app.payment-service.base-url=${PAYMENT_SERVICE_URL:http://localhost:8084}
```

Set `MONGODB_URI` as an environment variable (never commit secrets).

---

## Run

```powershell
cd ride-service
.\mvnw.cmd spring-boot:run
```

```bash
cd ride-service
./mvnw spring-boot:run
```

## Test

```powershell
.\mvnw.cmd test
```

---

## Example: create ride (Postman)

```http
POST http://localhost:8083/api/rides
Content-Type: application/json

{
  "passengerId": "PASS001",
  "pickupLocation": "Colombo Fort",
  "destinationLocation": "Kandy",
  "distanceKm": 10
}
```

Expected: **201 Created** with status `REQUESTED` and an estimated fare.
