# Stationery Management System

A full-stack **microservices-based** Stationery Management System built with **Spring Boot**, **Spring Cloud**, and **React**. The system enables organisations to manage stationery inventory, process issue requests with admin approval, and maintain complete audit trails — all through a modern, role-adaptive web interface.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND (React)                               │
│                            http://localhost:3000                             │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │  REACT_APP_API_URL=http://localhost:8090
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY (Spring Cloud)                          │
│                            http://localhost:8090                             │
│               Route: /api/auth/**       → AUTH-SERVICE                      │
│               Route: /api/inventory/**  → INVENTORY-SERVICE                 │
│               Route: /api/requests/**   → REQUEST-SERVICE                   │
│               Cross-Cutting: JWT Validation (GlobalFilter), CORS            │
└──────┬──────────────────────┬──────────────────────┬───────────────────────┘
       │                      │                      │
       ▼                      ▼                      ▼
┌──────────────┐   ┌───────────────────┐   ┌──────────────────┐
│ AUTH SERVICE │   │ INVENTORY SERVICE │   │ REQUEST SERVICE  │
│  :8081       │   │  :8082            │   │  :8083           │
│              │   │                   │   │                  │
│ • Register   │   │ • CRUD Items      │   │ • Create Request │
│ • Login/JWT  │   │ • Stock Mgmt      │   │ • Approve/Reject │
│ • Validate   │   │ • Category filter │   │ • Fulfill        │
│ • Roles      │   │ • Low-Stock Alert │   │ • Track by UUID  │
│              │   │ • Search          │   │ • Feign → Inv.   │
└──────┬───────┘   └────────┬──────────┘   └────────┬─────────┘
       │                    │                        │
       ▼                    ▼                        ▼
┌──────────────┐   ┌───────────────────┐   ┌──────────────────┐
│   auth_db    │   │  inventory_db     │   │   request_db     │
│   (MySQL)    │   │   (MySQL)         │   │   (MySQL)        │
└──────────────┘   └───────────────────┘   └──────────────────┘

                    ┌───────────────────┐
                    │  CONFIG SERVER    │◄── Centralised Configuration
                    │  :8888            │    (Classpath native profiles)
                    └───────────────────┘

                    ┌───────────────────┐
                    │  EUREKA SERVER    │◄── Service Discovery
                    │  :8761            │    & Registration
                    └───────────────────┘
```

---

## Technology Stack

| Layer              | Technology                                       |
|--------------------|--------------------------------------------------|
| **Frontend**       | React 18, React Router, Axios, Context API        |
| **API Gateway**    | Spring Cloud Gateway (Reactive, WebFlux)          |
| **Backend**        | Java 17, Spring Boot 3.2.0, Spring Cloud 2023.x   |
| **Security**       | Spring Security, JWT (HMAC-SHA256 via jjwt)       |
| **Service Comm.**  | OpenFeign (Declarative REST Client)               |
| **Discovery**      | Netflix Eureka (Service Registry)                 |
| **Configuration**  | Spring Cloud Config Server (Classpath / Native)   |
| **Database**       | MySQL 8.0 (Separate DB per microservice)          |
| **ORM**            | Spring Data JPA / Hibernate                       |
| **Validation**     | Jakarta Bean Validation (@Valid, @NotBlank, etc.) |
| **Build**          | Maven 3.9+ (Multi-module parent POM)              |
| **Containerisation** | Docker, Docker Compose                          |
| **CI/CD**          | Jenkins (Declarative Pipeline)                    |
| **Testing**        | JUnit 5, Mockito, JaCoCo (coverage reports)       |

---

## Prerequisites

| Tool               | Version  | Notes                                                        |
|--------------------|----------|--------------------------------------------------------------|
| **Java JDK**       | 17+      | [Eclipse Temurin](https://adoptium.net/)                     |
| **Maven**          | 3.9+     | [Maven Downloads](https://maven.apache.org/download.cgi)     |
| **Node.js**        | 18+      | [Node.js Downloads](https://nodejs.org/)                     |
| **MySQL**          | 8.0      | Required only for local (non-Docker) setup                   |
| **Docker**         | 24+      | [Docker Desktop](https://www.docker.com/products/docker-desktop) |
| **Docker Compose** | 2.20+    | Bundled with Docker Desktop                                  |
| **Git**            | 2.40+    | [Git Downloads](https://git-scm.com/downloads)               |

---

## Service Ports

| Service            | Port     | Host-mapped Port (Docker)         |
|--------------------|----------|-----------------------------------|
| MySQL              | 3306     | **3307** (host → container)       |
| Config Server      | 8888     | 8888                              |
| Eureka Server      | 8761     | 8761                              |
| **API Gateway**    | **8090** | 8090 ← All frontend traffic here  |
| Auth Service       | 8081     | 8081                              |
| Inventory Service  | 8082     | 8082                              |
| Request Service    | 8083     | 8083                              |
| Frontend (Nginx)   | 80       | **3000**                          |

> **Note:** The API Gateway runs on port **8090**, not 8080. The frontend is configured via `REACT_APP_API_URL=http://localhost:8090`.

---

## Running with Docker Compose (Recommended)

Docker Compose handles all startup ordering automatically via health checks.

```bash
# Clone the repository
git clone <repo-url>
cd stationery-managementFinal

# Build and start all 8 services
docker compose up --build

# Or run in detached mode
docker compose up --build -d

# View running containers
docker compose ps

# Follow logs for a specific service
docker compose logs -f request-service

# Stop all services
docker compose down

# Stop and remove data volumes (fresh start)
docker compose down -v
```

### Startup Order (enforced by health checks)

```
MySQL → Config Server → Eureka Server → API Gateway + Auth + Inventory → Request Service → Frontend
```

### Service Health Check URLs

| Service            | Health Check URL                              |
|--------------------|-----------------------------------------------|
| Config Server      | http://localhost:8888/actuator/health          |
| Eureka Dashboard   | http://localhost:8761                          |
| API Gateway        | http://localhost:8090/actuator/health          |
| Auth Service       | http://localhost:8081/actuator/health          |
| Inventory Service  | http://localhost:8082/actuator/health          |
| Request Service    | http://localhost:8083/actuator/health          |
| **Frontend**       | **http://localhost:3000**                      |

---

## Running Locally (Without Docker)

### Step 1: Create MySQL Databases

```sql
mysql -u root -p

CREATE DATABASE IF NOT EXISTS auth_db      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS request_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Start services in order

```bash
# 1. Config Server (port 8888)
cd config-server && mvn spring-boot:run

# 2. Eureka Server (port 8761)
cd eureka-server && mvn spring-boot:run

# 3. API Gateway (port 8090)
cd api-gateway && mvn spring-boot:run

# 4. Auth Service (port 8081)
cd auth-service && mvn spring-boot:run

# 5. Inventory Service (port 8082)
cd inventory-service && mvn spring-boot:run

# 6. Request Service (port 8083)
cd request-service && mvn spring-boot:run

# 7. Frontend (port 3000)
cd frontend && npm install && npm start
```

> Wait for each service to log its `Started ...Application on port XXXX` message before starting the next one.

---

## API Documentation

All API calls must go through the **API Gateway at `http://localhost:8090`**.

### Authentication Service — `/api/auth`

| Method | Endpoint              | Auth Required | Description                |
|--------|-----------------------|:-------------:|----------------------------|
| POST   | `/api/auth/register`  | No            | Register a new user        |
| POST   | `/api/auth/login`     | No            | Login and receive JWT token|
| GET    | `/api/auth/validate`  | Bearer Token  | Validate a JWT token       |

#### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "password": "secret123",
  "role": "STUDENT"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "alice",
  "role": "STUDENT",
  "message": "User registered successfully"
}
```

> `role` is optional — defaults to `STUDENT`. Valid values: `ADMIN`, `STUDENT`.

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "secret123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "alice",
  "role": "STUDENT",
  "message": "Login successful"
}
```

#### Validate Token

```http
GET /api/auth/validate
Authorization: Bearer <jwt-token>
```

**Response:** `200 OK` — `"Token is valid"` / `401 Unauthorized`

---

### Inventory Service — `/api/inventory`

| Method | Endpoint                             | Role           | Description                          |
|--------|--------------------------------------|----------------|--------------------------------------|
| GET    | `/api/inventory`                     | Any auth       | Get all items (paginated)            |
| GET    | `/api/inventory/{id}`                | Any auth       | Get item by ID                       |
| POST   | `/api/inventory`                     | ADMIN only     | Create new stationery item           |
| PUT    | `/api/inventory/{id}`                | ADMIN only     | Update item (field-level audit log)  |
| DELETE | `/api/inventory/{id}`                | ADMIN only     | Delete item                          |
| GET    | `/api/inventory/category/{category}` | Any auth       | Get items by category (paginated)    |
| GET    | `/api/inventory/low-stock`           | ADMIN only     | Get items where qty ≤ minimum        |
| GET    | `/api/inventory/search?keyword=`     | Any auth       | Case-insensitive keyword search      |
| PUT    | `/api/inventory/{id}/deduct`         | Internal only  | Deduct stock (called by Request Svc) |

**Pagination params (GET /api/inventory):** `?page=0&size=20&sortBy=name`

#### Create Item (ADMIN)

```http
POST /api/inventory
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Blue Ballpoint Pen",
  "category": "PEN",
  "unit": "pieces",
  "availableQuantity": 500,
  "minimumQuantity": 50,
  "description": "Standard blue ballpoint pen"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Blue Ballpoint Pen",
  "category": "PEN",
  "unit": "pieces",
  "availableQuantity": 500,
  "minimumQuantity": 50,
  "description": "Standard blue ballpoint pen",
  "lowStock": false,
  "createdAt": "2026-06-17T12:00:00",
  "updatedAt": "2026-06-17T12:00:00"
}
```

**Valid categories:** `PAPER`, `PEN`, `PENCIL`, `NOTEBOOK`, `ERASER`, `MARKER`, `FOLDER`, `STAPLER`, `OTHER`

---

### Request Service — `/api/requests`

| Method | Endpoint                        | Role       | Description                                        |
|--------|---------------------------------|------------|----------------------------------------------------|
| POST   | `/api/requests`                 | STUDENT    | Submit a new request (status = PENDING)            |
| GET    | `/api/requests/my`              | STUDENT    | View own requests (filter + sort supported)        |
| GET    | `/api/requests`                 | ADMIN      | View all requests (filter + sort supported)        |
| GET    | `/api/requests/{id}`            | Any auth   | Get request by database ID                         |
| GET    | `/api/requests/track/{requestId}` | Any auth | Track request by UUID                              |
| PUT    | `/api/requests/{id}/approve`    | ADMIN      | Approve (deducts inventory via Feign, atomically)  |
| PUT    | `/api/requests/{id}/reject`     | ADMIN      | Reject with optional reason                        |
| PUT    | `/api/requests/{id}/fulfill`    | ADMIN      | Mark an APPROVED request as physically handed over |

**Query params for list endpoints:** `?status=PENDING&sortBy=date&sortOrder=desc`  
Valid `sortBy` values: `date`, `status`  
Valid `status` values: `PENDING`, `APPROVED`, `REJECTED`, `FULFILLED`

#### Submit Request (STUDENT)

```http
POST /api/requests
Authorization: Bearer <student-token>
Content-Type: application/json

{
  "items": [
    { "itemId": 1, "itemName": "Blue Ballpoint Pen", "quantity": 10 },
    { "itemId": 2, "itemName": "A4 Notebook", "quantity": 3 }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "requestId": "a3f9b2c1-...",
  "studentUsername": "alice",
  "items": [
    { "itemId": 1, "itemName": "Blue Ballpoint Pen", "quantity": 10 },
    { "itemId": 2, "itemName": "A4 Notebook", "quantity": 3 }
  ],
  "status": "PENDING",
  "rejectionReason": null,
  "adminUsername": null,
  "createdAt": "2026-06-17T12:30:00",
  "updatedAt": "2026-06-17T12:30:00"
}
```

> A UUID `requestId` is auto-generated — use `/api/requests/track/{requestId}` for public-facing status tracking.

#### Approve Request (ADMIN)

```http
PUT /api/requests/1/approve
Authorization: Bearer <admin-token>
```

Stock is deducted from Inventory Service atomically. If stock is insufficient for any item, the approval is rejected with `400 Bad Request` and the request stays `PENDING`.

#### Reject Request (ADMIN)

```http
PUT /api/requests/1/reject
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "rejectionReason": "Budget constraints this quarter"
}
```

#### Fulfill Request (ADMIN)

```http
PUT /api/requests/1/fulfill
Authorization: Bearer <admin-token>
```

Transitions an `APPROVED` request to `FULFILLED` — represents physical handover to the student. Stock was already deducted at approval time.

---

## Database Schema

### auth_db

| Table   | Column         | Type          | Notes                              |
|---------|----------------|---------------|------------------------------------|
| `users` | `id`           | BIGINT PK     | Auto-increment                     |
|         | `username`     | VARCHAR UNIQUE| Not null                           |
|         | `email`        | VARCHAR UNIQUE| Not null, validated with @Email    |
|         | `password`     | VARCHAR       | BCrypt hash                        |
|         | `role`         | ENUM STRING   | `ADMIN` or `STUDENT`               |
|         | `created_at`   | DATETIME      | Set by @PrePersist                 |
|         | `updated_at`   | DATETIME      | Set by @PreUpdate                  |

### inventory_db

| Table              | Column               | Type       | Notes                          |
|--------------------|----------------------|------------|--------------------------------|
| `stationery_items` | `id`                 | BIGINT PK  | Auto-increment                 |
|                    | `name`               | VARCHAR    | Not null                       |
|                    | `category`           | VARCHAR    | Stored as string (e.g., `PEN`) |
|                    | `unit`               | VARCHAR    | e.g., `pieces`, `reams`        |
|                    | `available_quantity` | INT        | Not null, ≥ 0                  |
|                    | `minimum_quantity`   | INT        | Low-stock threshold            |
|                    | `description`        | TEXT       | Optional                       |
|                    | `created_at`         | DATETIME   |                                |
|                    | `updated_at`         | DATETIME   |                                |

### request_db

| Table                  | Column             | Type       | Notes                                   |
|------------------------|--------------------|------------|-----------------------------------------|
| `stationery_requests`  | `id`               | BIGINT PK  | Auto-increment (internal ID)            |
|                        | `request_id`       | VARCHAR UK | UUID, auto-generated in @PrePersist     |
|                        | `student_username` | VARCHAR    | Sourced from JWT X-User-Name header     |
|                        | `status`           | ENUM STRING| `PENDING`, `APPROVED`, `REJECTED`, `FULFILLED` |
|                        | `rejection_reason` | VARCHAR    | Populated on REJECT                     |
|                        | `admin_username`   | VARCHAR    | Populated on APPROVE/REJECT             |
|                        | `created_at`       | DATETIME   |                                         |
|                        | `updated_at`       | DATETIME   |                                         |
| `request_items`        | `id`               | BIGINT PK  |                                         |
|                        | `request_id`       | BIGINT FK  | → stationery_requests.id               |
|                        | `item_id`          | BIGINT     | References inventory_db item (by conv.) |
|                        | `item_name`        | VARCHAR    | Denormalised snapshot at request time   |
|                        | `quantity`         | INT        | Min value: 1                            |

---

## Project Structure

```
stationery-managementFinal/
│
├── pom.xml                        # Root multi-module Maven POM
├── docker-compose.yml             # Full-stack orchestration (8 containers)
├── init.sql                       # DB bootstrap: creates auth_db, inventory_db, request_db
├── Jenkinsfile                    # CI/CD pipeline
├── README.md
│
├── config-server/                 # Spring Cloud Config Server (:8888)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── configs/               # Per-service config files
│   │       ├── api-gateway.yml
│   │       ├── auth-service.yml
│   │       ├── inventory-service.yml
│   │       └── request-service.yml
│   └── Dockerfile
│
├── eureka-server/                 # Netflix Eureka Server (:8761)
│   ├── src/main/resources/application.yml
│   └── Dockerfile
│
├── api-gateway/                   # Spring Cloud Gateway (:8090)
│   ├── src/main/java/com/stationery/gateway/
│   │   ├── config/CorsConfig.java
│   │   └── filter/JwtAuthFilter.java   # GlobalFilter (order = -1)
│   ├── src/main/resources/application.yml
│   └── Dockerfile
│
├── auth-service/                  # Authentication & JWT (:8081)
│   ├── src/main/java/com/stationery/auth/
│   │   ├── controller/AuthController.java
│   │   ├── service/AuthService.java
│   │   ├── security/
│   │   │   ├── JwtUtil.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── CustomUserDetailsService.java
│   │   ├── model/User.java
│   │   ├── model/Role.java             # Enum: ADMIN, STUDENT
│   │   ├── dto/                        # RegisterRequest, LoginRequest, AuthResponse
│   │   ├── repository/UserRepository.java
│   │   └── exception/GlobalExceptionHandler.java
│   ├── src/test/                       # JUnit 5 + Mockito test suite
│   └── Dockerfile
│
├── inventory-service/             # Stationery catalogue & stock (:8082)
│   ├── src/main/java/com/stationery/inventory/
│   │   ├── controller/InventoryController.java
│   │   ├── service/InventoryService.java
│   │   ├── model/StationeryItem.java
│   │   ├── model/Category.java         # Enum: PAPER, PEN, PENCIL, NOTEBOOK, ...
│   │   ├── dto/                        # StationeryItemRequest, StationeryItemResponse
│   │   ├── repository/StationeryItemRepository.java
│   │   └── exception/GlobalExceptionHandler.java
│   ├── src/test/                       # JUnit 5 + Mockito test suite
│   └── Dockerfile
│
├── request-service/               # Procurement lifecycle (:8083)
│   ├── src/main/java/com/stationery/request/
│   │   ├── controller/RequestController.java
│   │   ├── service/RequestService.java
│   │   ├── client/InventoryClient.java  # Feign client → inventory-service
│   │   ├── model/StationeryRequest.java
│   │   ├── model/RequestItem.java
│   │   ├── model/RequestStatus.java    # Enum: PENDING, APPROVED, REJECTED, FULFILLED
│   │   ├── dto/                        # CreateRequestDto, RequestItemDto, RequestResponse, ApproveRejectDto
│   │   ├── repository/RequestRepository.java
│   │   └── exception/GlobalExceptionHandler.java
│   ├── src/test/                       # JUnit 5 + Mockito test suite
│   └── Dockerfile
│
└── frontend/                      # React SPA (Nginx in Docker → :3000)
    ├── src/
    │   ├── api/axiosConfig.js          # Axios instance with JWT interceptors
    │   ├── context/AuthContext.js      # Global auth state (React Context API)
    │   ├── components/
    │   │   ├── Layout.js / .css
    │   │   ├── Sidebar.js / .css
    │   │   ├── ProtectedRoute.js       # Role-based route guard
    │   │   ├── LoadingSpinner.js
    │   │   └── StatusStamp.js
    │   ├── pages/
    │   │   ├── Login.js
    │   │   ├── Register.js
    │   │   ├── Dashboard.js
    │   │   ├── Inventory.js            # Shared — diverges by role
    │   │   ├── AddItem.js              # ADMIN only
    │   │   ├── EditItem.js             # ADMIN only
    │   │   ├── CreateRequest.js        # STUDENT only
    │   │   ├── MyRequests.js           # STUDENT only
    │   │   └── ManageRequests.js       # ADMIN only
    │   └── App.js                      # Router with ProtectedRoute wrappers
    ├── .env                            # REACT_APP_API_URL=http://localhost:8090
    ├── Dockerfile                      # Multi-stage: Node build + Nginx serve
    └── package.json
```

---

## CI/CD Pipeline (Jenkinsfile)

```
┌──────────┐   ┌─────────────────┐   ┌───────────┐   ┌────────────────┐   ┌────────┐
│ Checkout │──▶│ Build Backend   │──▶│ Run Tests │──▶│ Build Frontend │──▶│ Deploy │
│  (SCM)   │   │ mvn clean pkg   │   │ mvn test  │   │ npm ci + build │   │ docker │
│          │   │ -DskipTests     │   │ + JaCoCo  │   │                │   │compose │
└──────────┘   └─────────────────┘   └───────────┘   └────────────────┘   └────────┘
```

| Stage              | Command                                    | Notes                                        |
|--------------------|--------------------------------------------|----------------------------------------------|
| **Checkout**       | `checkout scm`                             | Pulls latest from GitHub                     |
| **Build Backend**  | `mvn clean package -DskipTests`            | Builds all 6 Spring Boot JARs via root POM   |
| **Run Tests**      | `mvn test`                                 | JUnit 5 + Mockito; JaCoCo coverage generated |
| **Build Frontend** | `npm ci && npm run build`                  | Reproducible install; production build        |
| **Deploy**         | `docker compose down && docker compose up -d --build` | Full redeploy              |

---

## Running Tests

```bash
# Run tests for all backend modules from root
mvn test

# Run tests for a specific service
cd auth-service     && mvn test
cd inventory-service && mvn test
cd request-service  && mvn test

# Generate JaCoCo HTML coverage report
mvn test jacoco:report
# Report: target/site/jacoco/index.html
```

---

## Configuration & Environment Variables

All services load their configuration from **Config Server** at startup. Key variables can be overridden at runtime:

| Variable                                   | Default                                     | Description                  |
|--------------------------------------------|---------------------------------------------|------------------------------|
| `SPRING_PROFILES_ACTIVE`                   | `docker` (in Compose), `default` (local)    | Activates dev/prod/test config|
| `SPRING_CONFIG_IMPORT`                     | `configserver:http://config-server:8888`    | Config Server bootstrap URL  |
| `SPRING_DATASOURCE_URL`                    | `jdbc:mysql://mysql:3306/<db>`              | Per-service DB connection URL |
| `SPRING_DATASOURCE_USERNAME`               | `root`                                      | DB username                  |
| `SPRING_DATASOURCE_PASSWORD`               | `root`                                      | DB password                  |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`     | `http://eureka-server:8761/eureka/`         | Eureka server URL            |
| `JWT_SECRET`                               | `stationeryManagementSecretKey2024...`      | HMAC-SHA256 signing key      |
| `JWT_EXPIRATION`                           | `86400000` (24 hours)                       | Token expiry in ms           |
| `REACT_APP_API_URL`                        | `http://localhost:8090`                     | Frontend → Gateway base URL  |

Available config profiles per service: `dev`, `prod`, `test`, `docker`

---

## Troubleshooting

| Problem                                     | Solution                                                                      |
|---------------------------------------------|-------------------------------------------------------------------------------|
| Frontend shows "Network Error"              | Verify API Gateway is running on **port 8090** (not 8080)                     |
| Service can't connect to Config Server      | Ensure Config Server is healthy before starting other services                |
| `Connection refused` to MySQL               | Docker: MySQL exposes on host port **3307**, not 3306                         |
| Eureka dashboard shows no services          | Services take ~30s to register; wait and refresh                              |
| JWT token rejected at Gateway               | Ensure the same `jwt.secret` is in all service configs                        |
| Docker services keep restarting             | Check logs: `docker compose logs -f <service-name>`                           |
| Request approval fails with 400             | Insufficient stock — check inventory `availableQuantity` for requested items  |
| Port already in use                         | Change host port mapping in `docker-compose.yml`                              |

---

## PDF Requirement Coverage

| Requirement                                                  | Status     | Notes                                                              |
|--------------------------------------------------------------|------------|--------------------------------------------------------------------|
| FR-01: User Registration (email validation, BCrypt, dup. check) | ✅ Done | @Email, BCrypt, existsByUsername/Email checks                    |
| FR-02: JWT Authentication (24h token)                        | ✅ Done    | 86400000ms expiry; stateless JWT                                   |
| FR-03: RBAC — ADMIN and STUDENT roles                        | ✅ Done    | Enforced at gateway (JwtAuthFilter) and controller layer           |
| FR-04: Add Stationery Item (ADMIN, all required fields)      | ✅ Done    | POST /api/inventory with validation                                |
| FR-05: View Catalogue (students), Low-stock for admins, Pagination | ✅ Done | Paginated GET; low-stock endpoint; category filter             |
| FR-06: Update Item + Audit Logging                           | ✅ Done    | PUT /api/inventory/{id}; field-level AUDIT: log entries            |
| FR-07: Submit Request (multi-item, PENDING, UUID + timestamp) | ✅ Done  | POST /api/requests; UUID requestId; items list                     |
| FR-08: View My Requests (filter by status, sort by date/status) | ✅ Done | GET /api/requests/my with full filter + sort params             |
| FR-09: Approve/Reject + stock deduction + rejection reason   | ✅ Done    | PUT approve/reject/fulfill; Feign deduction; reason field          |
| Spring Cloud Gateway                                         | ✅ Done    | api-gateway with JwtAuthFilter                                     |
| Eureka Service Registry                                      | ✅ Done    | eureka-server; all services register                               |
| Config Server                                                | ✅ Done    | config-server; native classpath profiles                           |
| Feign inter-service communication                            | ✅ Done    | InventoryClient in request-service                                 |
| Spring Data JPA + Hibernate                                  | ✅ Done    | All three domain services                                          |
| JUnit + Mockito unit tests                                   | ✅ Done    | Multi-layer tests in all 3 services                                |
| JaCoCo coverage                                              | ✅ Done    | Configured in all service pom.xml files                            |
| Jenkins CI/CD Pipeline                                       | ✅ Done    | Jenkinsfile at root                                                |
| Dockerfile per microservice                                  | ✅ Done    | All 7 components (6 services + frontend)                           |
| Docker Compose                                               | ✅ Done    | docker-compose.yml with health-checked startup                     |
| Dev / Test / Prod environment configs                        | ✅ Done    | application-dev/prod/test.yml per service                          |
| React Frontend                                               | ✅ Done    | Role-adaptive SPA; ADMIN and STUDENT flows                         |
| Input validation + exception handling                        | ✅ Done    | @Valid, GlobalExceptionHandler, structured ErrorResponse           |
| Refresh token support                                        | ❌ Not implemented | Marked optional in PDF; JWT expiry is the only mechanism  |
| SonarQube integration                                        | ❌ Not implemented | Marked optional in PDF                                     |
| Notifications to student on approve/reject                   | ❌ Not implemented | Marked optional in PDF                                     |

---

## License

This project is developed as a Java Spring Boot Microservices Capstone Project.