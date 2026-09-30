# RideLink Microservices

RideLink is a backend microservices-based ride-sharing platform developed for the **IT3130 – Application Development Group Assignment**.

The system is implemented using **Java and Spring Boot** and is divided into four independently executable microservices. Each team member is the primary owner of one microservice, while the group is jointly responsible for integration, architecture, API contracts, testing, documentation, and demonstration.

---

## 1. Project Overview

RideLink simulates the backend of a ride-sharing platform.

The main workflow includes:

1. Passenger/driver account registration and authentication.
2. Driver and vehicle information management.
3. Driver availability and simulated location management.
4. Fare estimation.
5. Ride request creation and driver assignment.
6. Ride lifecycle management.
7. Final fare calculation.
8. Simulated payment processing.
9. Payment status tracking.
10. Receipt generation and retrieval.

No web or mobile frontend is required. The backend services are demonstrated using **Swagger/OpenAPI** and **Postman**.

---

## 2. Microservice Architecture

The system contains exactly four core microservices:

| # | Microservice | Default Port | Primary Responsibility | Database |
|---|---|---:|---|---|
| 1 | Account Service | 8081 | Registration, login, roles, profiles, account status | `ridelink_account_db` |
| 2 | Driver & Vehicle Service | 8082 | Driver profile, vehicle details, availability, service area, simulated location | `ridelink_driver_db` |
| 3 | Ride Management Service | 8083 | Ride requests, driver assignment, ride lifecycle, cancellation and retrieval | `ridelink_ride_db` |
| 4 | Fare & Payment Service | 8084 | Fare estimation, final fare calculation, simulated payment, payment status, receipt generation | `ridelink_payment_db` |

### Architecture Rule

Each microservice owns its own persistence boundary.

A service **must not directly query or modify another service's database**.

Correct:

```text
Ride Service
    |
    | REST API
    v
Driver Service
    |
    v
ridelink_driver_db
```

Incorrect:

```text
Ride Service
    |
    v
ridelink_driver_db
```

---

## 3. Repository Structure

```text
ridelink-microservices/
│
├── account-service/
│   ├── .mvn/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── driver-vehicle-service/
│   ├── .mvn/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── ride-service/
│   ├── .mvn/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── fare-payment-service/
│   ├── .mvn/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── docs/
│   ├── architecture/
│   ├── sequence-diagrams/
│   └── screenshots/
│
├── postman/
│
├── .github/
│   └── workflows/
│
├── .gitignore
└── README.md
```

---

## 4. Technology Stack

### Backend

- Java
- Spring Boot
- Maven
- Spring Web
- Spring Data MongoDB
- Spring Security
- Jakarta Bean Validation
- Lombok

### Database

- MongoDB Atlas

### API / Testing

- RESTful JSON APIs
- Postman
- Swagger / OpenAPI
- JUnit
- Mockito

### Version Control / Collaboration

- Git
- GitHub
- Feature branches
- Pull Requests
- Peer Review
- GitHub Actions CI

---

## 5. Development Versions

> Update the exact local tool versions before final submission by running the commands shown below.

| Tool | Project Version / Target | Verification Command |
|---|---|---|
| Java / JDK | Java 21 | `java -version` |
| Java Compiler | Java 21 | `javac -version` |
| Spring Boot | 4.1.1 | Check each service `pom.xml` |
| Maven | Maven Wrapper included per service | `.\mvnw.cmd -v` |
| Git | **Replace with your installed version** | `git --version` |
| MongoDB | MongoDB Atlas | Managed cloud service |
| Postman | Desktop application | Check Postman About |
| VS Code / IntelliJ | Developer choice | Check IDE About |

### Recommended Version Evidence

Run these commands and add the real outputs to the final report or screenshots:

```powershell
java -version
javac -version
git --version
.\mvnw.cmd -v
```

---

## 6. Prerequisites

Before running the project, install/configure:

- JDK 21
- Git
- VS Code or IntelliJ IDEA
- Postman
- MongoDB Atlas account
- Internet connection for MongoDB Atlas
- Maven is not required globally because each service includes the Maven Wrapper

Verify Java:

```powershell
java -version
javac -version
```

Verify Git:

```powershell
git --version
```

---

## 7. Clone the Repository

```bash
git clone https://github.com/disanayakaKG/ridelink-microservices.git
cd ridelink-microservices
```

Check remote:

```bash
git remote -v
```

---

## 8. Git Branching Workflow

This repository uses a shared Git workflow to clearly show individual contribution.

### Main Branches

```text
main
develop
```

`main`
- Stable and demonstrable project version.
- Should not be used for day-to-day development.

`develop`
- Integration branch.
- Approved feature branches are merged here first.

### Member Feature Branches

Recommended branch names:

```text
feature/account-service
feature/driver-service
feature/ride-service
feature/payment-service
```

### Workflow

```text
main
  ↑
  │ Final tested integration
  │
develop
  ↑
  │ Pull Request + Review
  │
feature/account-service
feature/driver-service
feature/ride-service
feature/payment-service
```

### Standard Member Workflow

Always start from the latest `develop`:

```bash
git checkout develop
git pull origin develop
```

Create your own feature branch:

```bash
git checkout -b feature/payment-service
```

Push the branch:

```bash
git push -u origin feature/payment-service
```

Work only on your assigned service unless an agreed integration change is required.

After making a meaningful change:

```bash
git status
git add .
git commit -m "feat: implement fare estimation"
git push
```

Create a Pull Request:

```text
base: develop
compare: feature/payment-service
```

Another group member should review the Pull Request before merging.

After merge:

```bash
git checkout develop
git pull origin develop
```

---

## 9. Commit Message Convention

Use meaningful commits.

Recommended examples:

```text
feat: implement fare estimation
feat: add simulated payment processing
feat: add payment status update endpoint
feat: add receipt retrieval
fix: prevent invalid payment status transition
test: add fare calculation unit tests
test: add payment service unit tests
docs: update payment API documentation
chore: configure MongoDB environment properties
```

Avoid vague commits such as:

```text
update
final
changes
work
test
abc
```

---

## 10. MongoDB Atlas Setup

All four services may use the same MongoDB Atlas cluster, but each service must use a **separate database**.

Recommended structure:

```text
Cluster0
│
├── ridelink_account_db
│   └── users
│
├── ridelink_driver_db
│   ├── drivers
│   └── vehicles
│
├── ridelink_ride_db
│   └── rides
│
└── ridelink_payment_db
    └── payments
```

### Database Ownership

```text
Account Service
→ ridelink_account_db

Driver & Vehicle Service
→ ridelink_driver_db

Ride Management Service
→ ridelink_ride_db

Fare & Payment Service
→ ridelink_payment_db
```

---

## 11. Environment Variables

Sensitive information must not be committed to GitHub.

Never commit:

- MongoDB usernames/passwords
- MongoDB connection strings
- JWT secrets
- API tokens
- Production credentials

Create a Windows environment variable:

```cmd
setx MONGODB_URI "mongodb+srv://<username>:<password>@<cluster-host>/?appName=Cluster0"
```

Close and reopen the terminal/IDE after using `setx`.

Verify in PowerShell:

```powershell
$env:MONGODB_URI
```

Verify in CMD:

```cmd
echo %MONGODB_URI%
```

Do not include the real URI in screenshots, reports, commits, or shared messages.

---

## 12. Service Configuration

### Account Service

`account-service/src/main/resources/application.properties`

```properties
spring.application.name=account-service
server.port=8081

spring.mongodb.uri=${MONGODB_URI}
spring.mongodb.database=ridelink_account_db
```

### Driver & Vehicle Service

`driver-vehicle-service/src/main/resources/application.properties`

```properties
spring.application.name=driver-vehicle-service
server.port=8082

spring.mongodb.uri=${MONGODB_URI}
spring.mongodb.database=ridelink_driver_db
```

### Ride Management Service

`ride-service/src/main/resources/application.properties`

```properties
spring.application.name=ride-service
server.port=8083

spring.mongodb.uri=${MONGODB_URI}
spring.mongodb.database=ridelink_ride_db
```

### Fare & Payment Service

`fare-payment-service/src/main/resources/application.properties`

```properties
spring.application.name=fare-payment-service
server.port=8084

spring.mongodb.uri=${MONGODB_URI}
spring.mongodb.database=ridelink_payment_db
```

---

## 13. Running the Services

Each service is independently executable.

### Account Service

```powershell
cd account-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8081
```

### Driver & Vehicle Service

```powershell
cd driver-vehicle-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8082
```

### Ride Management Service

```powershell
cd ride-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8083
```

### Fare & Payment Service

```powershell
cd fare-payment-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8084
```

Successful startup should contain messages similar to:

```text
Tomcat started on port 8084
Started FarePaymentServiceApplication
```

---

## 14. Fare & Payment Service

The Fare & Payment Service is responsible for:

- Fare estimation
- Final fare calculation
- Simulated payment creation
- Payment persistence
- Payment status management
- Payment history/retrieval
- Receipt generation/retrieval

Default port:

```text
8084
```

Database:

```text
ridelink_payment_db
```

Main package:

```text
com.ridelink.payment
```

---

## 15. Fare & Payment Service Package Structure

```text
com.ridelink.payment
│
├── config/
├── controller/
├── dto/
├── exception/
├── model/
├── repository/
├── service/
│   └── impl/
└── FarePaymentServiceApplication.java
```

Recommended purpose:

- `controller` — REST API endpoints
- `dto` — request/response objects
- `model` — MongoDB document models and enums
- `repository` — MongoDB repository interfaces
- `service` — business service interfaces
- `service/impl` — business logic implementations
- `exception` — custom exceptions and global error handling
- `config` — security/OpenAPI/application configuration

---

## 16. Fare Calculation Rule

Current documented fare rule:

```text
Base Fare       = Rs. 150
Rate Per KM     = Rs. 80

Estimated Fare  = Base Fare + (Distance KM × Rate Per KM)
```

Example:

```text
Distance = 10 KM

Fare = 150 + (10 × 80)
     = Rs. 950
```

The same calculation rule must be consistently implemented and documented.

---

## 17. Fare & Payment API Endpoints

The exact set may evolve during integration, but the Fare & Payment Service should support the following core operations.

### Fare Estimate

```http
POST /api/fares/estimate
```

Example request:

```json
{
  "distanceKm": 10
}
```

Example response:

```json
{
  "distanceKm": 10.0,
  "baseFare": 150.0,
  "perKmRate": 80.0,
  "estimatedFare": 950.0
}
```

### Create Payment

```http
POST /api/payments
```

Example request:

```json
{
  "rideId": "RIDE001",
  "passengerId": "PASS001",
  "amount": 950.00,
  "paymentMethod": "CARD"
}
```

Example successful response:

```json
{
  "id": "<mongo-generated-id>",
  "rideId": "RIDE001",
  "passengerId": "PASS001",
  "amount": 950.0,
  "paymentMethod": "CARD",
  "status": "PENDING",
  "createdAt": "<timestamp>"
}
```

Expected HTTP status:

```text
201 Created
```

### Get Payment by ID

```http
GET /api/payments/{id}
```

### Get Payment by Ride

```http
GET /api/payments/ride/{rideId}
```

### Update Payment Status

Recommended:

```http
PATCH /api/payments/{id}/status
```

Possible statuses:

```text
PENDING
SUCCESS
FAILED
```

### Receipt

Recommended:

```http
GET /api/payments/{id}/receipt
```

---

## 18. Payment Persistence Test

Start the Fare & Payment Service:

```powershell
cd fare-payment-service
.\mvnw.cmd spring-boot:run
```

Send using Postman:

```http
POST http://localhost:8084/api/payments
```

Body:

```json
{
  "rideId": "RIDE001",
  "passengerId": "PASS001",
  "amount": 950.00,
  "paymentMethod": "CARD"
}
```

Expected:

```text
201 Created
```

Then verify in MongoDB Atlas:

```text
Cluster0
→ ridelink_payment_db
→ payments
```

A newly generated MongoDB document ID confirms that persistence is working.

---

## 19. Payment Lifecycle

Recommended simulated payment lifecycle:

```text
PENDING
   |
   +----> SUCCESS
   |
   └----> FAILED
```

Invalid transitions should be handled consistently by the service.

---

## 20. Inter-Service Integration

The services communicate through APIs instead of sharing databases.

Example:

```text
Ride Management Service
        |
        | Request final fare / payment
        v
Fare & Payment Service
        |
        v
ridelink_payment_db
```

The Fare & Payment Service should use stable identifiers such as:

```text
rideId
passengerId
paymentId
```

These identifiers allow services to communicate without directly accessing one another's persistence layer.

---

## 21. Postman

The shared Postman collection should be stored under:

```text
postman/
```

Recommended structure:

```text
RideLink API
│
├── Account Service
├── Driver & Vehicle Service
├── Ride Management Service
├── Fare & Payment Service
├── End-to-End Workflow
└── Negative Scenarios
```

Recommended variables:

```text
account_url = http://localhost:8081
driver_url = http://localhost:8082
ride_url = http://localhost:8083
payment_url = http://localhost:8084

token =
passengerId =
driverId =
rideId =
paymentId =
```

---

## 22. Swagger / OpenAPI

Each REST service should expose accurate OpenAPI documentation.

Typical URL pattern:

```text
http://localhost:<port>/swagger-ui/index.html
```

Example Fare & Payment Service:

```text
http://localhost:8084/swagger-ui/index.html
```

Swagger should document:

- request body
- response body
- HTTP methods
- status codes
- validation rules
- error responses

---

## 23. Validation and Error Handling

Recommended validation examples:

- Distance must be greater than zero.
- `rideId` must not be blank.
- `passengerId` must not be blank.
- Amount must be positive.
- Payment method must be valid.
- Payment ID must exist before retrieval/status update.
- Invalid payment state transitions should return a suitable HTTP error.

Recommended HTTP responses:

```text
200 OK
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

---

## 24. Testing

Every service should contain meaningful unit tests.

### Fare & Payment Service Recommended Tests

```text
calculateFare_success
calculateFare_invalidDistance

createPayment_success
getPayment_success
getPayment_notFound

updatePaymentStatus_success
updatePaymentStatus_invalidTransition

generateReceipt_success
```

Run tests:

```powershell
.\mvnw.cmd test
```

---

## 25. End-to-End Workflow

Recommended complete demonstration:

```text
1. Passenger registers
          ↓
2. Passenger logs in
          ↓
3. Driver profile and vehicle are registered
          ↓
4. Driver becomes AVAILABLE
          ↓
5. Passenger requests fare estimate
          ↓
6. Passenger creates ride request
          ↓
7. Ride Service obtains eligible driver
          ↓
8. Driver is assigned
          ↓
9. Driver accepts ride
          ↓
10. Ride starts
          ↓
11. Ride completes
          ↓
12. Final fare is calculated
          ↓
13. Simulated payment is created
          ↓
14. Payment status is updated
          ↓
15. Receipt is retrieved
```

---

## 26. Ride Lifecycle

Recommended ride states:

```text
REQUESTED
    ↓
ASSIGNED
    ↓
ACCEPTED
    ↓
IN_PROGRESS
    ↓
COMPLETED
```

Cancellation may be allowed from selected states according to the agreed API contract.

---

## 27. Negative Scenarios

The integrated system should demonstrate failure scenarios.

Examples:

- No available driver
- Invalid ride status transition
- Unauthorized API access
- Invalid input
- Invalid payment amount
- Payment not found
- Simulated payment failure

---

## 28. Security

The final integrated solution should support authentication and role-based authorization where appropriate.

Recommended roles:

```text
PASSENGER
DRIVER
ADMIN
```

Security practices:

- Hash passwords using BCrypt.
- Use JWT or the group-agreed token mechanism.
- Never store plain-text passwords.
- Never commit secrets.
- Use environment variables.
- Apply authorization rules by role.
- Validate all external input.

---

## 29. CI / GitHub Actions

CI configuration should be stored in:

```text
.github/workflows/
```

The final CI pipeline should automatically build and test all four services.

Expected concept:

```text
Checkout Repository
        ↓
Setup Java
        ↓
Build Account Service
        ↓
Test Account Service
        ↓
Build Driver Service
        ↓
Test Driver Service
        ↓
Build Ride Service
        ↓
Test Ride Service
        ↓
Build Fare & Payment Service
        ↓
Test Fare & Payment Service
```

---

## 30. Team Integration Rules

Because the project is divided among four members, follow these rules:

1. Each member owns one primary microservice.
2. Each member works from their own GitHub identity.
3. Each member uses their own feature branch.
4. Do not directly modify another member's service without agreement.
5. Agree on API contracts before integration.
6. Never access another service's database directly.
7. Integrate through REST or another agreed inter-service mechanism.
8. Use Pull Requests before merging into `develop`.
9. Another member should review significant Pull Requests.
10. Keep `develop` working before merging into `main`.
11. All members must understand the complete integrated workflow.

---

## 31. Recommended Team Ownership

Update names before submission.

| Member | Microservice | Branch |
|---|---|---|
| Member 1 | Account Service | `feature/account-service` |
| Member 2 | Driver & Vehicle Service | `feature/driver-service` |
| Member 3 | Ride Management Service | `feature/ride-service` |
| Member 4 | Fare & Payment Service | `feature/payment-service` |

---

## 32. Individual Contribution Evidence

Each member should keep evidence of:

- own feature branch
- meaningful commits
- Pull Requests
- code reviews
- unit tests
- API documentation
- Postman testing
- screenshots
- design decisions
- service integration work

Do not rely only on commit count. Contributions should be meaningful and traceable.

---

## 33. Useful Commands

Check current branch:

```bash
git branch
```

Check changes:

```bash
git status
```

Get latest integration code:

```bash
git checkout develop
git pull origin develop
```

Create feature branch:

```bash
git checkout -b feature/payment-service
```

Stage changes:

```bash
git add .
```

Commit:

```bash
git commit -m "feat: implement simulated payment processing"
```

Push:

```bash
git push
```

Run a service:

```powershell
.\mvnw.cmd spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

Stop a running Spring Boot service:

```text
Ctrl + C
```

---

## 34. `.gitignore`

The root `.gitignore` should include at least:

```gitignore
# Maven build output
**/target/

# IDE
.idea/
*.iml
.vscode/

# Environment / secrets
.env
.env.*
application-local.properties

# Logs
*.log

# Operating system files
.DS_Store
Thumbs.db
```

---

## 35. Documentation

Supporting files should be kept under:

```text
docs/
├── architecture/
├── sequence-diagrams/
└── screenshots/
```

Recommended documentation:

- overall architecture diagram
- service responsibility diagram
- service/data ownership diagram
- end-to-end sequence diagram
- inter-service communication diagram
- MongoDB structure screenshot
- Postman success/negative scenario screenshots
- Git/Pull Request evidence
- CI workflow evidence

---

## 36. Startup Order

A practical local startup order is:

```text
1. Account Service          :8081
2. Driver & Vehicle Service :8082
3. Fare & Payment Service   :8084
4. Ride Management Service  :8083
```

When integration code is added, ensure all services required by a workflow are running before testing the workflow.

---

## 37. Current Development Status

Update this section as the project progresses.

### Repository / Architecture

- [x] Shared GitHub repository created
- [x] `main` branch available
- [x] `develop` branch available
- [x] Four Spring Boot service projects created
- [x] Separate MongoDB databases created
- [x] Service ports assigned
- [ ] Complete CI pipeline
- [ ] Final architecture documentation
- [ ] Final integration testing

### Fare & Payment Service

- [x] Fare & Payment Spring Boot project created
- [x] Service configured on port 8084
- [x] MongoDB Atlas connectivity configured
- [x] `ridelink_payment_db` created
- [x] Real payment persistence verified
- [x] Payment creation returns `201 Created`
- [ ] Complete payment status lifecycle
- [ ] Complete receipt endpoint
- [ ] Complete final fare endpoint
- [ ] Complete Swagger documentation
- [ ] Complete unit tests
- [ ] Complete Ride Service integration

---

## 38. Contribution / Authors

Update with actual member details before submission.

```text
Member 1:
Name:
Student ID:
Service: Account Service

Member 2:
Name:
Student ID:
Service: Driver & Vehicle Service

Member 3:
Name:
Student ID:
Service: Ride Management Service

Member 4:
Name:
Student ID:
Service: Fare & Payment Service
```

---

## 39. Academic / Development Notes

This project is developed as part of the IT3130 Application Development group assignment.

All members are expected to understand:

- the complete architecture
- their own source code
- service boundaries
- API contracts
- database ownership
- inter-service communication
- testing
- Git workflow
- integration decisions

Any external tools, libraries, or permitted AI-assisted development should be documented as required by the relevant academic-integrity rules.

---

## 40. Repository

```text
https://github.com/disanayakaKG/ridelink-microservices
```

---

## 41. Final Checklist

Before submission:

- [ ] Four services run independently
- [ ] Each service uses its own database
- [ ] Required APIs work
- [ ] Inter-service communication works
- [ ] Successful end-to-end workflow demonstrated
- [ ] Negative scenarios demonstrated
- [ ] Authentication / authorization completed
- [ ] Validation and exception handling completed
- [ ] Swagger/OpenAPI completed
- [ ] Postman collection exported
- [ ] Unit tests completed
- [ ] CI pipeline passes
- [ ] README updated
- [ ] Architecture diagram completed
- [ ] Sequence diagram completed
- [ ] Technical report completed
- [ ] Git contributions verified
- [ ] No secrets committed
- [ ] Release/tag created for assessed version
- [ ] Teaching team repository access confirmed
