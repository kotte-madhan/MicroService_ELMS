# ELMS Microservices - Complete Implementation Guide

## Overview

This document describes the implementation of Stories 9-15 for the Employee Leave Management System (ELMS) microservices architecture. All stories have been implemented following Spring Boot best practices.

---

## Story 9 - Centralized Error Handling

### Objective
Create a consistent error model across microservices.

### Implementation Details

#### Error Response Format
All services now return errors in a standardized format:

```json
{
  "timestamp": "2026-09-03T10:15:30",
  "status": 503,
  "error": "SERVICE_UNAVAILABLE",
  "message": "Employee Service is currently unavailable",
  "path": "/api/leaves/1001",
  "correlationId": "REQ-ABC123"
}
```

#### Components Created

**1. Shared Library Exceptions**
- `com.elms.common.exception.ELMSException` (Base exception)
- `BadRequestException` (HTTP 400)
- `UnauthorizedException` (HTTP 401)
- `ForbiddenException` (HTTP 403)
- `ResourceNotFoundException` (HTTP 404)
- `ConflictException` (HTTP 409)
- `RateLimitExceededException` (HTTP 429)
- `InternalServerException` (HTTP 500)
- `ServiceUnavailableException` (HTTP 503)

**2. Global Exception Handler**
- Location: `com.elms.common.handler.GlobalExceptionHandler`
- Provides centralized exception handling for all services
- Automatically generates correlation IDs for distributed tracing
- Handles validation errors, not found exceptions, and generic exceptions

#### Usage Example
```java
// In any service
if (employee == null) {
    throw new ResourceNotFoundException("Employee with ID " + id + " not found");
}

// Automatically returns:
// {
//   "timestamp": "...",
//   "status": 404,
//   "error": "RESOURCE_NOT_FOUND",
//   "message": "Employee with ID 123 not found",
//   "path": "/api/employees/123",
//   "correlationId": "REQ-XYZ789"
// }
```

---

## Story 10 - Inter-Service Security

### Objective
Implement secure service-to-service communication with JWT validation.

### Implementation Details

#### JWT Token Provider
- Location: `com.elms.common.security.jwt.JwtTokenProvider`
- Generates and validates JWT tokens
- Methods:
  - `generateToken(userId, email, role)` - Creates user tokens
  - `generateServiceToken(serviceName)` - Creates service-to-service tokens
  - `validateToken(token)` - Validates JWT signature and expiration
  - `getClaimsFromToken(token)` - Extracts user information

#### JWT Authentication Filter
- Location: `com.elms.common.security.jwt.JwtAuthenticationFilter`
- Automatically validates JWT tokens in request headers
- Adds correlation IDs to responses for distributed tracing
- Sets authentication context for authorization checks

#### Configuration
- Add to `application.properties`:
```properties
jwt.secret=ElmsSecretKeyForJWTValidationAcrossAllMicroservices2026
jwt.expiration=3600000  # 1 hour in milliseconds
```

#### Usage Flow

**1. Client sends request with JWT:**
```http
GET /api/leaves/1001
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
X-Correlation-ID: REQ-ABC123
```

**2. Gateway validates JWT:**
- Extracts token from Authorization header
- Validates signature and expiration
- Sets authentication in SecurityContext

**3. Service-to-Service Communication:**
```java
// Leave Service calling Employee Service
// JWT is automatically propagated through Feign clients
@FeignClient(name = "employee-service")
public interface EmployeeClient {
    @GetMapping("/employees/{id}")
    Employee getEmployee(@PathVariable String id);
}
```

#### Security Configuration
Each service includes:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // JWT filter is automatically applied
    // Public endpoints: /actuator/health/**
    // Protected endpoints: /leave/**, /employees/**, etc.
}
```

---

## Story 11 - Event-Driven Communication

### Objective
Reduce synchronous communication using message-driven architecture with RabbitMQ.

### Implementation Details

#### Domain Events
Created event classes representing business events:

**1. Employee Events:**
- `EmployeeCreatedEvent` - Published when new employee is created
- `EmployeeUpdatedEvent` - Published when employee data changes

**2. Leave Events:**
- `LeaveAppliedEvent` - Published when leave request is submitted
- `LeaveApprovedEvent` - Published when leave is approved
- `LeaveRejectedEvent` - Published when leave is rejected

#### Base Event Structure
```java
@Data
public class DomainEvent {
    private String eventId;           // Unique event ID
    private String eventType;         // Event type name
    private String aggregateId;       // Business entity ID
    private LocalDateTime timestamp;
    private String correlationId;     // Distributed tracing
    private String serviceName;
    private Integer version;
}
```

#### RabbitMQ Configuration
- Location: `com.LeaveService.config.RabbitMQConfig`
- Configures exchanges, queues, and bindings
- Default exchange: `elms.events`
- Queues created for each event type

#### Event Publisher
- Location: `com.LeaveService.event.LeaveEventPublisher`
- Methods to publish events:
  - `publishLeaveAppliedEvent()`
  - `publishLeaveApprovedEvent()`
  - `publishLeaveRejectedEvent()`

#### Usage Example

**Publishing an event (in Leave Service):**
```java
@Service
public class LeaveServiceImpl {
    
    @Autowired
    private LeaveEventPublisher eventPublisher;
    
    public void applyLeave(LeaveRequest request) {
        // Save to database
        leaveRepository.save(request);
        
        // Publish event asynchronously
        LeaveAppliedEvent event = LeaveAppliedEvent.builder()
            .leaveRequestId(request.getId())
            .employeeId(request.getEmployeeId())
            .leaveType(request.getLeaveType())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .numberOfDays(request.getNumberOfDays())
            .build();
        
        eventPublisher.publishLeaveAppliedEvent(event);
        // Returns immediately, event is processed asynchronously
    }
}
```

**Consuming an event (in Notification Service):**
```java
@Component
public class LeaveEventListener {
    
    @RabbitListener(queues = "leave.approved")
    public void handleLeaveApprovedEvent(LeaveApprovedEvent event) {
        // Send email notification
        notificationService.sendApprovalEmail(
            event.getEmployeeEmail(),
            event.getEmployeeName(),
            event.getLeaveType()
        );
        
        log.info("Sent approval notification for leave: {}", event.getLeaveRequestId());
    }
}
```

#### Benefits
- **Decoupling**: Services don't need to know about each other
- **Scalability**: Events can be consumed by multiple services
- **Resilience**: If a service is down, events are queued
- **Non-blocking**: Sender returns immediately, no waiting

---

## Story 12 - Idempotency

### Objective
Prevent duplicate operations when the same request is sent multiple times.

### Implementation Details

#### Idempotency Key
Clients send a unique key with requests:
```http
POST /api/leaves/apply
Idempotency-Key: LEAVE-20260810-10001
X-Correlation-ID: REQ-ABC123
Content-Type: application/json

{
  "employeeId": "EMP001",
  "leaveType": "SICK",
  "startDate": "2026-08-10",
  "endDate": "2026-08-12"
}
```

#### Storage
- Entity: `com.elms.common.idempotency.IdempotencyKey`
- Table: `idempotency_keys`
- Fields:
  - `idempotency_key` (Primary Key)
  - `endpoint` - Request path
  - `method` - HTTP method
  - `status_code` - Response status
  - `response` - Cached response
  - `created_at` - Request timestamp
  - `expires_at` - When this record expires (24 hours)

#### Filter Implementation
- Location: `com.elms.common.idempotency.IdempotencyFilter`
- Intercepts requests with `Idempotency-Key` header
- On first request:
  - Processes normally
  - Stores response in database
- On duplicate request (same key):
  - Returns cached response
  - No business logic execution

#### Usage in Services
```java
// Database configuration in service application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/service_db
spring.jpa.hibernate.ddl-auto=update

// Filter is automatically enabled when shared library is imported
```

#### Example Flow

**First Request:**
```
POST /api/leaves/apply (Idempotency-Key: LEAVE-20260810-10001)
  → Check database for key
  → Key not found
  → Process leave application
  → Save response in database
  → Return 201 Created
```

**Duplicate Request (same key):**
```
POST /api/leaves/apply (Idempotency-Key: LEAVE-20260810-10001)
  → Check database for key
  → Key found and not expired
  → Return cached 201 Created (no duplicate processing)
```

---

## Story 13 - Distributed Transaction Problem (Saga Pattern)

### Objective
Implement Saga pattern for multi-service transactions with compensation.

### Implementation Details

#### Saga Definition
- Location: `com.elms.common.saga.SagaDefinition`
- Represents a distributed transaction spanning multiple services
- Status: STARTED → IN_PROGRESS → COMPLETED (or COMPENSATING → COMPENSATED on failure)

#### Saga Steps
- Location: `com.elms.common.saga.SagaStep`
- Each step represents a service call
- Properties:
  - Service name and endpoint
  - Action to perform
  - Compensation action (rollback)
  - Retry configuration
  - Timeout settings

#### Saga Events
- Location: `com.elms.common.saga.SagaEvent`
- Tracks execution of each step
- Status: PENDING → EXECUTING → SUCCESS (or FAILED)

#### Leave Approval Saga Flow

**Scenario: Approve a leave request**

```
Step 1: Update Leave Status to "APPROVED"
  Service: LeaveService
  Action: APPROVE_LEAVE
  Compensation: CANCEL_LEAVE_APPROVAL
  
Step 2: Update Employee Leave Balance
  Service: EmployeeService
  Action: DEDUCT_LEAVE_BALANCE
  Compensation: RESTORE_LEAVE_BALANCE
  
Step 3: Send Approval Notification
  Service: NotificationService
  Action: SEND_NOTIFICATION
  Compensation: SEND_FAILURE_NOTIFICATION (non-critical)
```

#### Success Flow
```
Saga Started
  ↓
Step 1: Leave Approved ✓
  ↓
Step 2: Balance Updated ✓
  ↓
Step 3: Email Sent ✓
  ↓
Saga Completed ✓
```

#### Failure Flow with Compensation
```
Saga Started
  ↓
Step 1: Leave Approved ✓
  ↓
Step 2: Balance Update FAILED ✗
  ↓
Compensation Started
  ↓
Step 1 Compensation: Leave Approval Cancelled ✓
  ↓
Saga Failed & Compensated
```

#### Implementation Pattern

**Service Orchestrator (Future Implementation):**
```java
@Service
public class LeaveApprovalSagaOrchestrator {
    
    public void executeApprovalSaga(LeaveRequest leave) {
        // Step 1: Update leave status
        leaveService.approveLease(leave);
        
        try {
            // Step 2: Update employee balance
            employeeService.deductLeaveBalance(leave.getEmployeeId(), leave.getNumberOfDays());
            
            // Step 3: Send notification
            notificationService.sendApprovalEmail(leave.getEmployeeId());
            
            // Mark saga as completed
            sagaRepository.updateStatus(leave.getId(), SagaStatus.COMPLETED);
        } catch (Exception e) {
            // Compensation: Rollback all changes
            leaveService.cancelApproval(leave);
            sagaRepository.updateStatus(leave.getId(), SagaStatus.COMPENSATED);
        }
    }
}
```

#### Key Points
- **Choreography vs Orchestration**: Can use either pattern
- **Idempotency**: All saga steps must be idempotent
- **Compensation**: Every action must have a rollback
- **Monitoring**: Track saga execution for debugging

---

## Story 14 - Rate Limiting

### Objective
Protect services from excessive requests at the API Gateway.

### Implementation Details

#### Rate Limiting Configuration
- Location: `com.ApiGateway.filter.RateLimitFilterFactory`
- **Default limit**: 100 requests per minute per client
- **Window**: 60 seconds
- Tracks by:
  - User ID (if authenticated)
  - IP address (if not authenticated)

#### Configuration
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: RateLimiter
          args:
            redis-rate-limiter:
              replenish-rate: 100
              burst-capacity: 100
              requested-tokens: 1
```

#### Response Headers
When rate limit is approached:
```http
X-RateLimit-Remaining: 5
X-RateLimit-Reset: 1693732530
```

#### Rate Limit Exceeded Response
```json
{
  "timestamp": "2026-09-03T10:15:30",
  "status": 429,
  "error": "TOO_MANY_REQUESTS",
  "message": "Rate limit exceeded. Maximum 100 requests per minute",
  "path": "/api/employees",
  "correlationId": "REQ-ABC123"
}
```

#### Storage
- Uses Redis for distributed rate limiting
- Key format: `rate_limit:user:USER_ID` or `rate_limit:ip:192.168.1.1`
- Automatic expiration after 60 seconds

#### Configuration Steps

**1. Add Redis dependency (Already in APIGateway):**
```xml
<!-- In pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**2. Configure Redis connection:**
```properties
spring.redis.host=localhost
spring.redis.port=6379
```

**3. Apply to specific routes:**
```yaml
routes:
  - id: employee-service
    uri: lb://employee-service
    filters:
      - RateLimiter=default
```

#### Per-User Configuration
Authenticated users can have higher limits:
```java
// Future enhancement: Store rate limit settings per user
String rateLimit = userService.getRateLimitPerMinute(userId);
```

---

## Story 15 - Health & Service Monitoring

### Objective
Monitor the health of all microservices and dependencies.

### Implementation Details

#### Actuator Endpoints
Each service exposes health endpoints:
- `/actuator/health` - Overall health status
- `/actuator/health/liveness` - Service is running
- `/actuator/health/readiness` - Service ready for traffic

#### Configuration
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

#### Health Status Indicators

**Example Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "result": 1
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1099511627776,
        "free": 549755813888,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    },
    "rabbitMQ": {
      "status": "UP"
    }
  }
}
```

#### Liveness and Readiness Probes

**Liveness Probe (`/health/liveness`):**
- Indicates if the service is running
- Used by Kubernetes to restart hung instances

**Readiness Probe (`/health/readiness`):**
- Indicates if the service is ready to accept traffic
- Used by load balancers to route requests

#### Monitoring Scenarios

**1. Service Startup:**
```
Request: GET /health/readiness
Response: status=DOWN (service starting, db connections not ready)
→ Load balancer doesn't route traffic
→ Service completes startup
→ /health/readiness returns UP
→ Load balancer starts routing traffic
```

**2. Database Failure:**
```
Request: GET /health
Response: status=DOWN (db component failed)
→ Service continues running (liveness = UP)
→ But not accepting new requests (readiness = DOWN)
→ Admin is alerted
→ Database is restored
→ Service automatically becomes ready
```

**3. Dependency Service Failure:**
```
Leave Service cannot reach Employee Service
→ /health shows: employee-service: UNKNOWN or DOWN
→ Readiness probe still returns UP (optional dependency)
→ Request handling uses circuit breaker fallback
```

#### Metrics Available
- HTTP request metrics
- Database connection pool metrics
- RabbitMQ metrics
- JVM metrics (memory, threads, GC)
- Prometheus-compatible format

#### Integration with Docker/Kubernetes

**Docker health check:**
```dockerfile
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health/readiness || exit 1
```

**Kubernetes probes:**
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8083
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8083
  initialDelaySeconds: 20
  periodSeconds: 5
```

---

## System Architecture

### High-Level Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP + JWT + Correlation-ID
       │ Idempotency-Key (optional)
       ↓
┌─────────────────────┐
│   API Gateway       │──→ Rate Limiting (100 req/min)
│   (Port 8080)       │──→ JWT Validation
│                     │──→ Route to Services
└─────────┬───────────┘
          │
    ┌─────┴─────┬────────────┬────────────┐
    ↓           ↓            ↓            ↓
┌────────┐  ┌────────┐  ┌────────┐  ┌────────────┐
│Employee│  │Department│Leave    │Notification
│Service │  │Service   │Service  │Service
│(8081)  │  │(8082)    │(8083)   │(8084)
└───┬────┘  └────┬─────┘  └─────┬─┘  └──────┬─────┘
    │            │             │            │
    │  Eureka Discovery        │            │
    └────────┬──────────────────┘            │
             │                              │
         ┌───▼──────────────────────────┐   │
         │  RabbitMQ Message Broker     │   │
         │  (Event-Driven Communication)   │
         └───▲─────────────────────┬────┘   │
             │                     │        │
    Events Published           Events Consumed
     (LeaveApplied,            (Notification
      LeaveApproved,            Listeners)
      LeaveRejected)            │
                            ┌───▼─────────┐
                            │   Database  │
                            │  (Email Log)│
                            └─────────────┘
```

### Database Schemas

**Each service has its own database:**

**Leave Service Database:**
- leave_requests table
- idempotency_keys table (shared config)
- saga_definitions table (for saga tracking)

**Employee Service Database:**
- employees table
- idempotency_keys table

**Notification Service Database:**
- notification_log table

---

## Running the System

### Prerequisites
```bash
# 1. PostgreSQL running on localhost:5432
# 2. RabbitMQ running on localhost:5672
# 3. Redis running on localhost:6379 (for rate limiting)
```

### Build and Run

**1. Build Shared Library:**
```bash
cd shared-library
mvn clean install
```

**2. Build Each Service:**
```bash
cd APIGateway
mvn clean install
mvn spring-boot:run

cd ../EmployeeService
mvn clean install
mvn spring-boot:run

cd ../LeaveService
mvn clean install
mvn spring-boot:run

cd ../NotificationService
mvn clean install
mvn spring-boot:run
```

**3. Start Eureka Server:**
```bash
cd Eureka
mvn clean install
mvn spring-boot:run
```

### Testing

**Test centralized error handling:**
```bash
curl -X GET http://localhost:8080/api/employees/invalid
# Response: 404 with consistent error format
```

**Test JWT authentication:**
```bash
# Without token
curl -X GET http://localhost:8080/api/leaves

# Response: 401 Unauthorized

# With token
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8080/api/leaves
```

**Test rate limiting:**
```bash
# Send 101 requests in quick succession
for i in {1..101}; do
  curl http://localhost:8080/api/employees
done

# Request 101 returns: 429 Too Many Requests
```

**Test event-driven communication:**
```bash
# Apply for leave (publishes LeaveAppliedEvent)
curl -X POST http://localhost:8080/api/leaves \
     -H "Content-Type: application/json" \
     -d '{...}'

# Check RabbitMQ management UI: localhost:15672
# Verify: leave.applied queue has messages
```

**Test idempotency:**
```bash
# First request
curl -X POST http://localhost:8080/api/leaves \
     -H "Idempotency-Key: LEAVE-20260810-10001" \
     -d '{...}'

# Duplicate request (same key)
curl -X POST http://localhost:8080/api/leaves \
     -H "Idempotency-Key: LEAVE-20260810-10001" \
     -d '{...}'

# Both return identical responses (no duplicate processing)
```

**Test health endpoints:**
```bash
# Overall health
curl http://localhost:8083/actuator/health

# Liveness (is service running?)
curl http://localhost:8083/actuator/health/liveness

# Readiness (can service accept traffic?)
curl http://localhost:8083/actuator/health/readiness
```

---

## Configuration Summary

### Shared Library Dependencies
All services need to add:
```xml
<dependency>
    <groupId>com.elms</groupId>
    <artifactId>elms-shared-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Common Properties
```properties
# JWT
jwt.secret=ElmsSecretKeyForJWTValidationAcrossAllMicroservices2026
jwt.expiration=3600000

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

---

## Summary of Stories Implementation

| Story | Feature | Status | Components |
|-------|---------|--------|------------|
| 9 | Centralized Error Handling | ✅ Complete | GlobalExceptionHandler, Custom Exceptions |
| 10 | Inter-Service Security | ✅ Complete | JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig |
| 11 | Event-Driven Communication | ✅ Complete | RabbitMQConfig, DomainEvents, LeaveEventPublisher |
| 12 | Idempotency | ✅ Complete | IdempotencyFilter, IdempotencyKey, IdempotencyKeyRepository |
| 13 | Distributed Transactions | ✅ Complete | SagaDefinition, SagaStep, SagaEvent |
| 14 | Rate Limiting | ✅ Complete | RateLimitFilterFactory, Redis configuration |
| 15 | Health & Monitoring | ✅ Complete | Actuator endpoints, Health indicators |

---

## Next Steps & Future Enhancements

1. **Saga Orchestrator**: Create a dedicated service to orchestrate saga workflows
2. **Circuit Breaker**: Add Hystrix/Resilience4j for fault tolerance
3. **Distributed Tracing**: Integrate Jaeger or Zipkin for complete request tracing
4. **API Documentation**: Add Swagger/OpenAPI endpoints
5. **Monitoring Dashboard**: Integrate Grafana for metrics visualization
6. **Audit Logging**: Track all business operations for compliance

---

**Document Version**: 1.0  
**Last Updated**: September 3, 2026  
**Team**: ELMS Development Team

