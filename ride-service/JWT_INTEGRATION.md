# Driver and Ride JWT integration

Driver and Ride validate Account tokens; neither issues tokens. Account and Fare/Payment authentication and production MongoDB configuration are unchanged.

## Contract

- Header: `Authorization: Bearer <token>`.
- `JWT_SECRET` is required in Driver and Ride, without a default. Supply exactly the same value to all four services, including Account (which still has its existing development fallback).
- Secret is raw UTF-8, not Base64-decoded. Keys shorter than 32 bytes are zero-padded to 32 bytes, matching Account. Algorithm is HS256 for 32-47 bytes, HS384 for 48-63 bytes, HS512 for 64+ bytes.
- Signature, `iss=ridelink-account`, `exp`, nonblank `sub`, and the string `role` are validated. Expiry has zero clock-skew allowance, matching Payment; `nbf` is also validated when present.
- Only PASSENGER, DRIVER, ADMIN are accepted and mapped to ROLE_PASSENGER, ROLE_DRIVER, ROLE_ADMIN. Principal name is `sub`.
- Stateless; CSRF, form login, HTTP Basic, logout, and request caching disabled. No generated development user/password.

## Routes

| Service | Public routes |
| --- | --- |
| Driver | `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` |
| Ride | `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api-docs/**`, `/actuator/**`, `/error` (existing exceptions preserved) |

Both permit internal ERROR dispatches so application errors retain their status. Permitting a path does not create an endpoint. An invalid Bearer token can still be rejected on public routes; use No Auth when testing public access.

| Service | Protected routes | Roles |
| --- | --- | --- |
| Driver | `GET /api/drivers/available` | Any accepted role |
| Driver | `POST /api/drivers`; `GET /api/drivers/{id}`; `GET /api/drivers/account/{accountId}`; `PATCH /api/drivers/{id}/availability`; `PATCH /api/drivers/{id}/location` | DRIVER or ADMIN |
| Ride | `POST /api/rides`; `GET /api/rides/{rideId}`; `GET /api/rides/id/{id}`; `GET /api/rides/passenger/{passengerId}`; `GET /api/rides/driver/{driverId}`; `POST /api/rides/{rideId}/assign`, `/accept`, `/start`, `/complete`, `/cancel` | Any accepted role |
| Both | All remaining routes | Authentication required |

No new ownership checks or Ride role restrictions were introduced. Existing ID-based business operations are preserved.

## Forwarding and verification

Ride's DriverServiceClient and PaymentServiceClient now use the configured RestClient builder. An interceptor resolves the authenticated JWT from SecurityContext on each synchronous call and sets the same bearer token on requests to the configured Driver/Payment base URLs. It does not cache a caller token or generate a service token. This covers available-driver lookup, fare estimation, and payment creation. Calls outside an authenticated context carry no token; future background/async jobs would need an explicit authentication design.

Commands run from the respective service directories: `.\mvnw.cmd test -B -ntp`.

- Driver: 37 tests, 0 failures, 0 errors, 0 skipped.
- Ride: 39 tests, 0 failures, 0 errors, 0 skipped.
- Tests use JJWT 0.12.6, matching Account, to sign real tokens. They cover key padding and all three algorithms, no/malformed/expired tokens, wrong signature/issuer, missing or invalid required claims, role mapping, Driver restrictions, statelessness, CSRF-free POSTs, disabled Basic/generated users, public Swagger, and downstream forwarding without cross-request token retention.
- HTTP security tests mock business services; existing service unit tests remain intact. MongoDB test properties point to localhost test databases. Local MongoDB was unavailable during the runs, producing background connection warnings; tests passed without database operations.
- Live four-service/Postman execution was not performed. Ride's existing completion flow catches payment failure and still completes the ride. Verify a non-null paymentId and the payment record, not only a 200 completion response.

## Exact manual Postman steps

1. Start Account (8081), Driver (8082), Ride (8083), Payment (8084) with their databases available. Configure the identical `JWT_SECRET` in each process environment before starting it. For local testing, override Ride's Mongo connection through `SPRING_MONGODB_URI` and `SPRING_MONGODB_DATABASE` if needed. Do not use a different per-service secret. No secret needs to be placed in Postman.
2. Create a Postman environment with `account=http://localhost:8081`, `driver=http://localhost:8082`, `ride=http://localhost:8083`, `payment=http://localhost:8084`. Select it. Set JSON bodies to Body > raw > JSON. For authenticated requests select Authorization > Bearer Token and enter the indicated variable without an additional `Bearer ` prefix.
3. Register a fresh passenger: `POST {{account}}/api/v1/auth/register`, No Auth, body:
   ```json
   {"email":"jwt.passenger@example.test","password":"LocalTestPass123!","fullName":"JWT Passenger","role":"PASSENGER"}
   ```
   Expect 201. Save response `token` as `passengerToken` and `userId` as `passengerId`. If already registered, `POST {{account}}/api/v1/auth/login` with `{"email":"jwt.passenger@example.test","password":"LocalTestPass123!"}` and expect 200; save the same fields.
4. Repeat registration with email `jwt.driver@example.test`, fullName `JWT Driver`, role `DRIVER`, same local test password. Save `driverToken` and `driverAccountId`. For an ADMIN test use an existing authorized test admin login and save `adminToken`.
5. With No Auth and no manually set Authorization header, send `GET {{driver}}/api/drivers/available` and `GET {{ride}}/api/rides/passenger/{{passengerId}}`. Both must return 401 with a Bearer challenge. Repeat with Bearer Token `not-a-jwt`; both must return 401. Basic Auth must also return 401.
6. Repeat both GETs using `{{passengerToken}}`. Expect 200 and JSON arrays (possibly empty). Repeat using `{{driverToken}}` and, if available, `{{adminToken}}`; expect 200.
7. Send `POST {{driver}}/api/drivers` with `{{driverToken}}` and body:
   ```json
   {"accountId":"{{driverAccountId}}","licenseNumber":"JWT-LICENSE-001","vehicleMake":"Toyota","vehicleModel":"Prius","vehiclePlateNumber":"JWT-001","serviceArea":"Colombo"}
   ```
   Expect 201; save response `id` as `driverId`. For an existing profile, fetch `GET {{driver}}/api/drivers/account/{{driverAccountId}}` with the driver token and save `id`.
8. Send `GET {{driver}}/api/drivers/{{driverId}}` with passenger token: expect 403. With driver token: expect 200. With admin token if available: expect 200. This checks role authorization. Send `PATCH {{driver}}/api/drivers/{{driverId}}/availability?status=AVAILABLE` with driver token, no body: expect 200. No CSRF token is needed.
9. With passenger token, send `POST {{ride}}/api/rides`:
   ```json
   {"passengerId":"{{passengerId}}","pickupLocation":"Colombo Fort","destinationLocation":"Nugegoda","distanceKm":10}
   ```
   Expect 201; save response `rideId` as `rideId`. Estimated fare should be 950 under the current fare rule. This request calls Payment's fare estimate (which already permits anonymous access).
10. With passenger token, send `POST {{ride}}/api/rides/{{rideId}}/assign` with `{"driverId":"{{driverId}}"}`. Expect 200 and status ASSIGNED. This invokes Driver's protected available-driver lookup using the forwarded passenger token.
11. With driver token, sequentially send these POSTs with no body: `{{ride}}/api/rides/{{rideId}}/accept`, then `/start`, then `/complete`. Expect 200 and statuses ACCEPTED, IN_PROGRESS, COMPLETED. Completion forwards the driver token to Payment; verify `paymentId` is non-null. Save it as `paymentId`.
12. Send `GET {{payment}}/api/payments/{{paymentId}}` with driver token. Expect 200, matching rideId, passengerId, and amount 950. This confirms payment actually succeeded; Ride's existing error handling can otherwise mask a failed payment.
13. With No Auth, request `GET {{driver}}/v3/api-docs` and `GET {{ride}}/v3/api-docs`: expect 200. Open `/swagger-ui.html` on each service; expect a redirect to Swagger UI (Postman may follow it automatically).
14. Expiry: in a local Account process only, set `JWT_EXPIRATION_MS=1000` and restart Account, leaving the shared secret unchanged. Log in, save `token` as `expiredToken`, wait at least 2 seconds, then use it on both GETs from step 5. Both must return 401. Restore Account's normal expiry and log in again for subsequent testing. Do not edit a JWT payload to simulate expiry, since that tests signature failure instead.
15. Signature tampering: copy a valid token, replace its entire signature segment (the part after the last dot) with `AAAA`, and send it to both protected GETs. Expect 401. Wrong issuer/missing claims are additionally covered by the automated real-token tests.

## Files changed

Paths below are relative to each service; Java package paths use `com/ridelink/driver` or `com/ridelink/ride`.

- Both: `pom.xml`; `src/main/resources/application.properties`; main `config/SecurityConfig.java`; new main `config/JwtConfig.java`; existing application context test; new test `security/AccountJwtTestSupport.java`, `security/JwtContractTest.java`, `security/JwtSecurityTest.java`.
- Driver: removed main `config/JwtService.java` and `config/JwtAuthenticationFilter.java`, replaced by the resource-server decoder/filter chain.
- Ride: main `config/RestClientConfig.java`, `client/DriverServiceClient.java`, `client/PaymentServiceClient.java`; new test `client/TokenForwardingTest.java`; this `JWT_INTEGRATION.md`.
