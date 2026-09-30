# ELMS Microservices - Quick Start Guide

## Project Structure

```
ELMS/
├── shared-library/          # Shared utilities and common components
│   ├── pom.xml
│   └── src/main/java/com/elms/common/
│       ├── dto/            # Error responses
│       ├── exception/       # Custom exceptions (400, 401, 403, 404, 409, 429, 500, 503)
│       ├── handler/         # Global exception handler
│       ├── security/jwt/    # JWT authentication
│       ├── event/           # Domain events
│       ├── idempotency/     # Idempotency mechanism
│       ├── saga/            # Saga pattern
│       ├── health/          # Health indicators
│       └── util/            # Correlation ID utilities
│
├── APIGateway/              # API Gateway (Port 8080)
│   ├── pom.xml
│   ├── src/
│   │   └── main/
│   │       ├── java/com/ApiGateway/
│   │       │   └── filter/  # Rate limiting filter
│   │       └── resources/
│   │           └── application.yml
│   
├── EmployeeService/         # Employee Management (Port 8081)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/EmployeeService/
│       │   ├── config/      # Security configuration
│       │   ├── controller/
│       │   ├── service/
│       │   ├── entity/
│       │   └── repository/
│       └── resources/
│           └── application.properties
│
├── DepartmentService/       # Department Management (Port 8082)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/DepartmentService/
│       │   ├── config/      # Security configuration
│       │   ├── controller/
│       │   ├── service/
│       │   ├── entity/
│       │   └── repository/
│       └── resources/
│           └── application.properties
│
├── LeaveService/            # Leave Management (Port 8083)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/LeaveService/
│       │   ├── config/      # Security & RabbitMQ config
│       │   ├── event/       # Event publisher
│       │   ├── controller/
│       │   ├── service/
│       │   ├── entity/
│       │   └── repository/
│       └── resources/
│           └── application.properties
│
├── NotificationService/     # Email Notifications (Port 8084)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/NotificationService/
│       │   ├── config/      # Security & RabbitMQ config
│       │   ├── event/       # Event listeners
│       │   ├── service/
│       │   ├── entity/
│       │   └── repository/
│       └── resources/
│           └── application.properties
│
├── Eureka/                  # Service Discovery (Port 8761)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ErukaServer/
│       └── resources/
│           └── application.properties
│
└── IMPLEMENTATION_GUIDE.md  # Detailed implementation documentation
```

---

## Prerequisites

### Required Software
```bash
# 1. Java 21+
java -version

# 2. Maven 3.8+
mvn -version

# 3. PostgreSQL 14+
psql --version

# 4. RabbitMQ 3.12+
# 5. Redis 7.0+
```

### Database Setup
```sql
-- Create databases for each service
CREATE DATABASE employee_db;
CREATE DATABASE department_db;
CREATE DATABASE leave_db;
CREATE DATABASE notification_db;
CREATE DATABASE elms_gateway;

-- Verify
\l
```

### RabbitMQ Setup
```bash
# Start RabbitMQ
rabbitmq-server

# Access Management UI
# URL: http://localhost:15672
# Username: guest
# Password: guest
```

### Redis Setup
```bash
# Start Redis
redis-server

# Test connection
redis-cli ping
# Response: PONG
```

---

## Build & Run Instructions

### 1. Build Shared Library (Required First)
```bash
cd shared-library
mvn clean install
# Output: elms-shared-library-1.0.0.jar installed in local Maven repository
```

### 2. Start Eureka Server
```bash
cd Eureka
mvn clean install
mvn spring-boot:run
# Service will be available at http://localhost:8761/eureka
```

### 3. Start Backend Services
Open 4 separate terminals and run:

**Terminal 1: Employee Service**
```bash
cd EmployeeService
mvn clean install
mvn spring-boot:run
# Port: 8081
# Check: http://localhost:8081/actuator/health
```

**Terminal 2: Department Service**
```bash
cd DepartmentService
mvn clean install
mvn spring-boot:run
# Port: 8082
# Check: http://localhost:8082/actuator/health
```

**Terminal 3: Leave Service**
```bash
cd LeaveService
mvn clean install
mvn spring-boot:run
# Port: 8083
# Check: http://localhost:8083/actuator/health
```

**Terminal 4: Notification Service**
```bash
cd NotificationService
mvn clean install
mvn spring-boot:run
# Port: 8084
# Check: http://localhost:8084/actuator/health
```

### 4. Start API Gateway
```bash
cd APIGateway
mvn clean install
mvn spring-boot:run
# Port: 8080
# Gateway URL: http://localhost:8080
# Routes traffic to all services
```

### Verify All Services Are Running
```bash
# Check Eureka discovery
curl http://localhost:8761/eureka/apps

# Check Gateway
curl http://localhost:8080/actuator/health

# Check each service
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

---

## Testing the Implementation

### Story 9: Centralized Error Handling

**Test 400 Bad Request:**
```bash
curl -X POST http://localhost:8080/api/leaves \
  -H "Content-Type: application/json" \
  -d '{"invalid": "data"}'

# Response:
# {
#   "timestamp": "2026-09-03T10:15:30",
#   "status": 400,
#   "error": "BAD_REQUEST",
#   "message": "Validation failed",
#   "path": "/api/leaves",
#   "correlationId": "REQ-ABC123"
# }
```

**Test 404 Not Found:**
```bash
curl http://localhost:8080/api/employees/nonexistent

# Response: 404 with consistent error format
```

**Test 503 Service Unavailable:**
```bash
# Stop a service, then try to access it
# Response: 503 SERVICE_UNAVAILABLE
```

---

### Story 10: Inter-Service Security

**Generate JWT Token (for testing):**
```bash
# Use a tool like https://jwt.io or:
# Create a simple test endpoint in Employee Service

# Token format:
# Header: {"alg": "HS512", "typ": "JWT"}
# Payload: {
#   "userId": "emp-001",
#   "email": "emp@example.com",
#   "role": "EMPLOYEE",
#   "iat": 1693731330,
#   "exp": 1693734930
# }
```

**Test with JWT:**
```bash
# Without token - should be rejected
curl http://localhost:8080/api/leaves
# Response: 401 Unauthorized

# With token
curl -H "Authorization: Bearer <JWT_TOKEN>" \
  http://localhost:8080/api/leaves
# Response: 200 OK (if employee has leaves)
```

**Test Correlation ID Propagation:**
```bash
curl -H "X-Correlation-ID: REQ-12345" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  http://localhost:8080/api/leaves

# Response headers include:
# X-Correlation-ID: REQ-12345
```

---

### Story 11: Event-Driven Communication

**Check RabbitMQ Management UI:**
```
URL: http://localhost:15672
Username: guest
Password: guest
```

**Apply for Leave (publishes event):**
```bash
curl -X POST http://localhost:8080/api/leaves \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "employeeId": "emp-001",
    "leaveType": "SICK",
    "startDate": "2026-09-10",
    "endDate": "2026-09-12",
    "numberOfDays": 3,
    "reason": "Medical appointment"
  }'

# Response: 201 Created
# Leave Service publishes: LeaveAppliedEvent
```

**Monitor RabbitMQ:**
1. Go to http://localhost:15672
2. Click "Queues and Streams"
3. Check `leave.applied` queue for published events
4. Check `leave.approved` and `leave.rejected` queues

**Notification Listener logs:**
```bash
# In NotificationService terminal, you should see:
# Received LeaveAppliedEvent: <leave-request-id>
# Notification logged successfully for: employee@email.com
```

---

### Story 12: Idempotency

**First Request (new leave):**
```bash
curl -X POST http://localhost:8080/api/leaves \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Idempotency-Key: LEAVE-20260810-10001" \
  -d '{
    "employeeId": "emp-001",
    "leaveType": "VACATION",
    "startDate": "2026-08-10",
    "endDate": "2026-08-15"
  }'

# Response: 201 Created
# Database: idempotency_keys table stores response
```

**Duplicate Request (same key):**
```bash
curl -X POST http://localhost:8080/api/leaves \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Idempotency-Key: LEAVE-20260810-10001" \
  -d '{
    "employeeId": "emp-001",
    "leaveType": "VACATION",
    "startDate": "2026-08-10",
    "endDate": "2026-08-15"
  }'

# Response: 201 Created (IDENTICAL to first response)
# Database: No duplicate leave request created
```

**Verify in Database:**
```sql
-- Check idempotency keys
SELECT * FROM idempotency_keys 
WHERE idempotency_key = 'LEAVE-20260810-10001';

-- Check leave requests (should be only 1, not 2)
SELECT * FROM leave_requests 
WHERE employee_id = 'emp-001' AND start_date = '2026-08-10';
```

---

### Story 13: Distributed Transactions (Saga Pattern)

**Note: Saga orchestrator implementation is in progress**

Current implementation provides:
- `SagaDefinition` - Transaction definition
- `SagaStep` - Individual steps
- `SagaEvent` - Execution tracking

**Future saga flow:**
```
Approve Leave Request
  ↓
Step 1: Update Leave Status ✓
  ↓
Step 2: Deduct Leave Balance ✓
  ↓
Step 3: Send Email ✓
  ↓
Saga Completed ✓

# If Step 2 fails:
  ↓
Step 1 Compensation: Revert Leave Status ✓
  ↓
Saga Failed & Compensated
```

---

### Story 14: Rate Limiting

**Send 101 requests in quick succession:**
```bash
for i in {1..101}; do
  echo "Request $i:"
  curl -w "\nStatus: %{http_code}\n" \
    http://localhost:8080/api/employees
  sleep 0.01
done
```

**Expected Results:**
```
Requests 1-100: Status 200 OK
Request 101: Status 429 Too Many Requests

Response for request 101:
{
  "timestamp": "2026-09-03T10:15:30",
  "status": 429,
  "error": "TOO_MANY_REQUESTS",
  "message": "Rate limit exceeded. Maximum 100 requests per minute",
  "path": "/api/employees",
  "correlationId": "REQ-XYZ789"
}
```

**Response Headers:**
```
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1693732530
```

**Reset after 60 seconds:**
```bash
# Wait 60 seconds, then:
curl http://localhost:8080/api/employees
# Status: 200 OK (counter reset)
```

---

### Story 15: Health & Service Monitoring

**Overall Health:**
```bash
curl http://localhost:8083/actuator/health

# Response:
{
  "status": "UP",
  "components": {
    "db": {"status": "UP", "details": {...}},
    "diskSpace": {"status": "UP", "details": {...}},
    "ping": {"status": "UP"},
    "rabbitMQ": {"status": "UP"}
  }
}
```

**Liveness Probe (is service running?):**
```bash
curl http://localhost:8083/actuator/health/liveness

# Response: {"status": "UP"}
```

**Readiness Probe (can service accept traffic?):**
```bash
curl http://localhost:8083/actuator/health/readiness

# Response: {"status": "UP"}
```

**Metrics:**
```bash
curl http://localhost:8083/actuator/metrics

# Available metrics:
# - jvm.memory.used
# - jvm.threads.live
# - http.server.requests
# - db.connection.pool.usage
```

**Detailed Health (with authorization header):**
```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
  http://localhost:8083/actuator/health

# Returns detailed information about each component
```

**Kubernetes/Docker Integration:**
```yaml
# Kubernetes health probes
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

## API Endpoints Reference

### Employee Service (Port 8081)
```
POST   /employees              - Create employee
GET    /employees              - List all employees
GET    /employees/{id}         - Get employee by ID
PUT    /employees/{id}         - Update employee
DELETE /employees/{id}         - Delete employee
```

### Department Service (Port 8082)
```
POST   /departments            - Create department
GET    /departments            - List all departments
GET    /departments/{id}       - Get department by ID
PUT    /departments/{id}       - Update department
DELETE /departments/{id}       - Delete department
```

### Leave Service (Port 8083)
```
POST   /leave/apply            - Apply for leave
GET    /leave                  - Get all leave requests
GET    /leave/{id}             - Get leave request by ID
PUT    /leave/{id}/approve     - Approve leave request
PUT    /leave/{id}/reject      - Reject leave request
PUT    /leave/{id}/cancel      - Cancel leave request
```

### Notification Service (Port 8084)
```
POST   /notifications          - Send notification (manual)
GET    /notifications          - Get all notifications
GET    /notifications/{id}     - Get notification by ID
```

### API Gateway (Port 8080)
All above endpoints accessible through:
```
http://localhost:8080/api/employees
http://localhost:8080/api/departments
http://localhost:8080/api/leaves
http://localhost:8080/api/notifications
```

---

## Common Issues & Solutions

### Issue 1: Database Connection Failed
**Error:** `org.postgresql.util.PSQLException: Connection to localhost:5432 refused`

**Solution:**
```bash
# Start PostgreSQL
# macOS
brew services start postgresql

# Ubuntu/Debian
sudo service postgresql start

# Windows
# Open PostgreSQL in Services or start via pgAdmin
```

### Issue 2: RabbitMQ Connection Failed
**Error:** `java.net.ConnectException: Connection refused`

**Solution:**
```bash
# Start RabbitMQ
rabbitmq-server

# Or as service
# macOS
brew services start rabbitmq

# Ubuntu/Debian
sudo service rabbitmq-server start
```

### Issue 3: Redis Connection Failed
**Error:** `io.lettuce.core.RedisConnectionException`

**Solution:**
```bash
# Start Redis
redis-server

# Test connection
redis-cli ping
# Expected: PONG
```

### Issue 4: JWT Token Expired
**Error:** `401 Unauthorized - JWT signature invalid`

**Solution:**
```bash
# Generate a new token
# Token expires after 1 hour (configurable in application.properties)
jwt.expiration=3600000

# Update or create new token
```

### Issue 5: Service Not Found in Eureka
**Error:** Service doesn't appear in Eureka dashboard

**Solution:**
```bash
# 1. Verify service is running
curl http://localhost:8081/actuator/health

# 2. Check Eureka discovery URL in application.properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# 3. Wait 30 seconds for heartbeat
# 4. Check Eureka UI: http://localhost:8761
```

---

## Troubleshooting Commands

```bash
# Check if port is in use
lsof -i :8080
netstat -tulpn | grep :8080  # Linux
Get-NetTcpConnection -LocalPort 8080  # Windows PowerShell

# View logs
tail -f logs/leave-service.log

# Test service directly
curl -v http://localhost:8083/actuator/health

# Check database tables
psql -U postgres -d leave_db -c "\dt"

# Check RabbitMQ queues
# UI: http://localhost:15672
# Or CLI:
sudo rabbitmqctl list_queues

# Test Redis connection
redis-cli PING

# View Maven dependencies
mvn dependency:tree
```

---

## Project Configuration Files

### Key Configuration Properties

**shared-library/pom.xml**
- Defines shared dependencies (JWT, Spring Security, Spring Data JPA)
- Version: 1.0.0

**Application Properties (All Services)**
```properties
# Database (service-specific)
spring.datasource.url=jdbc:postgresql://localhost:5432/{service}_db

# JWT (all services)
jwt.secret=ElmsSecretKeyForJWTValidationAcrossAllMicroservices2026
jwt.expiration=3600000

# RabbitMQ (all services)
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672

# Eureka Discovery (all services)
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Actuator (all services)
management.endpoints.web.exposure.include=health,info,metrics
```

---

## Performance Tuning

### Connection Pooling
```properties
# In application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### RabbitMQ Consumers
```properties
# In RabbitMQConfig
# Increase concurrency for better throughput
@RabbitListener(queues = "...", concurrency = "3-10")
```

### Caching
```properties
# Already configured
spring.cache.type=simple

# For production, use Redis:
spring.cache.type=redis
```

---

## Documentation

- **Full Implementation Guide**: `IMPLEMENTATION_GUIDE.md`
- **Individual Stories**: See section "Story 9-15" in main guide
- **Code Comments**: Extensively commented in all Java files
- **API Documentation**: Add Swagger annotations for auto-generated docs (future enhancement)

---

## Next Steps

1. **Add API Documentation**: Integrate Swagger/OpenAPI
2. **Implement Circuit Breaker**: Enhance resilience4j configuration
3. **Distributed Tracing**: Integrate Jaeger/Zipkin
4. **Monitoring Dashboard**: Add Grafana + Prometheus
5. **Production Deployment**: Docker + Kubernetes configuration

---

**Last Updated**: September 3, 2026  
**Version**: 1.0.0  
**Team**: ELMS Development Team

