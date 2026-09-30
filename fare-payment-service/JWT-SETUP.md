# Fare & Payment JWT setup

This service validates tokens; only Account Service issues them. No login or
token-generation endpoint is added here.

## Account contract

Source inspected: `origin/feature/account-core`, commit `eb9ea04`,
`account-service/src/main/java/com/ridelink/account/security/JwtService.java`.
The checked-out Account Service on main does not yet contain that implementation.

- Shared HMAC secret encoded as raw UTF-8, not Base64-decoded.
- Account Service pads keys shorter than 32 bytes with zero bytes. This verifier
  mirrors that behavior for compatibility.
- JJWT 0.12.6 selects HS256 for keys below 48 bytes, HS384 for 48–63 bytes,
  and HS512 for 64 or more bytes. The verifier pins that same algorithm based on
  the configured secret. The deployed algorithm depends on the owner's secret.
- `sub`: user ID; `role`: one of `PASSENGER`, `DRIVER`, `ADMIN`;
  `email`: email; `iss`: `ridelink-account`; `iat` and `exp`: JWT timestamps.
- Account Service uses `JWT_EXPIRATION_MS`, default 3600000 (one hour), at issuance.
  Fare & Payment checks the token's `exp`, not a separate local lifetime.
- Signature, issuer, expiration, subject and role are validated. Expiration has
  zero clock-skew allowance, matching Account Service. Keep host clocks synchronized.
- Authorities map directly to `ROLE_PASSENGER`, `ROLE_DRIVER`, `ROLE_ADMIN`.

## Local configuration

Set `JWT_SECRET` in the environment of the process launching this service to
exactly the same value used by Account Service. Obtain it through your team's
private configuration channel; do not commit it, print it, or paste it into chat.
`jwt.secret=${JWT_SECRET}` has no default. Startup fails when it is missing.

`JWT_ISSUER` is optional and defaults to `ridelink-account`. Change it only if
the Account owner confirms a different issuer. MongoDB configuration is unchanged.

Launch from this directory with `.\mvnw.cmd spring-boot:run`. Port remains 8084.

## Endpoint access

| Access | Endpoint |
| --- | --- |
| Public | `POST /api/fares/estimate` |
| Public framework metadata | `GET /.well-known/oauth-protected-resource/**` |
| JWT required | `POST /api/payments` |
| JWT required | `GET /api/payments` |
| JWT required | `GET /api/payments/{id}` |
| JWT required | `GET /api/payments/ride/{rideId}` |
| JWT required | `GET /api/payments/passenger/{passengerId}` |
| JWT required | `PATCH /api/payments/{id}/status` |
| JWT required | `GET /api/payments/{id}/receipt` |
| JWT required | `POST /api/payments/fare/calculate` |

All other application requests require authentication. Internal ERROR dispatches
are permitted so existing error handling continues to work. Spring Security
also exposes its standard public OAuth protected-resource metadata endpoint.
A supplied invalid bearer token returns 401, including on public fare estimation.
Authentication is stateless; form login, HTTP Basic, CSRF, logout and request
caching are disabled. A JwtDecoder bean prevents generated-password user setup.

All three valid Account roles can access the authenticated payment endpoints.
This change adds JWT authentication and authority mapping, not new ownership or
role-specific payment policies.

## Postman

1. Obtain a token through Account Service `POST /api/v1/auth/login`.
2. Open a request such as `GET http://localhost:8084/api/payments/{id}/receipt`.
3. Authorization → Type **Bearer Token** → Token: paste only the JWT, without
   the `Bearer ` prefix. Postman sends `Authorization: Bearer <JWT>`.
4. Use `Content-Type: application/json` for JSON POST/PATCH requests.
5. For public fare estimation, choose **No Auth**.

## Verification

Run `.\mvnw.cmd test`. Security tests reproduce Account Service signing with
JJWT 0.12.6 and randomly generated test secrets; no production key is required.
Test contexts override MongoDB to localhost and never use the configured remote
database. Existing payment, receipt, validation and fare tests are retained.

Confirm with the Account owner that the deployed implementation matches the
inspected commit, obtain the shared secret privately, and confirm the login URL
and any intended role/ownership restrictions before cross-service deployment.
