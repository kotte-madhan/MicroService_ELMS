# ELMS Microservices - Startup Order & Dependencies

## Executive Summary

The ELMS project consists of 6 services plus infrastructure components. Services should be started in a specific order to ensure all dependencies are available.

---

## 📋 Infrastructure Prerequisites (Start First)

These must be running **BEFORE** starting any services:

### 1. **PostgreSQL Database** ⭐ CRITICAL
```bash
# Check if running
psql -U postgres -c "SELECT 1"

# Create databases for each service
psql -U postgres -c "CREATE DATABASE employee_db;"
psql -U postgres -c "CREATE DATABASE department_db;"
psql -U postgres -c "CREATE DATABASE leave_db;"
psql -U postgres -c "CREATE DATABASE notification_db;"
psql -U postgres -c "CREATE DATABASE elms_gateway;"
```

**Status Check**: `http://localhost:5432` (no direct HTTP, but psql can verify)

---

### 2. **RabbitMQ Message Broker** ⭐ CRITICAL
```bash
# Start RabbitMQ
rabbitmq-server

# Or on macOS
brew services start rabbitmq

# Or on Ubuntu/Debian
sudo service rabbitmq-server start
```

**Status Check**: 
- Management UI: `http://localhost:15672` (guest/guest)
- Direct: `telnet localhost 5672`

**Required for services**: LeaveService, NotificationService, EmployeeService

---

### 3. **Redis Cache** ⭐ CRITICAL
```bash
# Start Redis
redis-server

# Test connection
redis-cli ping
# Expected response: PONG
```

**Status Check**: 
- `redis-cli ping` → should return PONG
- Port: 6379

**Required for services**: APIGateway (rate limiting), All services (caching)

---

## 🚀 Application Startup Sequence

### Phase 1: Build Shared Library (One-Time)

```bash
cd shared-library
mvn clean install
```

**Output**: Installs `elms-shared-library-1.0.0.jar` in local Maven repository

**Why First**: All other services depend on this JAR

---

### Phase 2: Start Eureka Server (Service Discovery)

```bash
cd Eureka
mvn clean install
mvn spring-boot:run
```

**Port**: 8761
**UI**: `http://localhost:8761`
**Wait for**: "Started EurekaServerApplication in X seconds"

**Why Second**: All services register with Eureka on startup

---

### Phase 3: Start Backend Services (Parallel OK)

All backend services can start **simultaneously** or sequentially. No dependencies on each other.
Open 4 separate terminals:

#### Terminal 1: EmployeeService
```bash
cd EmployeeService
mvn clean install
mvn spring-boot:run
```
- **Port**: 8081
- **Check**: `curl http://localhost:8081/actuator/health`
- **Wait for**: "Started EmployeeServiceApplication in X seconds"
- **Eureka Registration**: 30 seconds to appear on dashboard

#### Terminal 2: DepartmentService
```bash
cd DepartmentService
mvn clean install
mvn spring-boot:run
```
- **Port**: 8082
- **Check**: `curl http://localhost:8082/actuator/health`
- **Wait for**: "Started DepartmentService in X seconds"
- **Eureka Registration**: 30 seconds

#### Terminal 3: LeaveService
```bash
cd LeaveService
mvn clean install
mvn spring-boot:run
```
- **Port**: 8083
- **Check**: `curl http://localhost:8083/actuator/health`
- **Wait for**: "Started LeaveServiceApplication in X seconds"
- **Eureka Registration**: 30 seconds
- **Dependencies**: RabbitMQ (for DLQ handler), PostgreSQL

#### Terminal 4: NotificationService
```bash
cd NotificationService
mvn clean install
mvn spring-boot:run
```
- **Port**: 8084
- **Check**: `curl http://localhost:8084/actuator/health`
- **Wait for**: "Started NotificationServiceApplication in X seconds"
- **Eureka Registration**: 30 seconds
- **Dependencies**: RabbitMQ (for event listeners), PostgreSQL

---

### Phase 4: Start API Gateway (Last)

```bash
cd APIGateway
mvn clean install
mvn spring-boot:run
```

**Port**: 8080
**UI**: `http://localhost:8080`
**Check**: `curl http://localhost:8080/actuator/health`
**Why Last**: Depends on all backend services being available to route traffic

---

## 📊 Complete Timeline

```
Time  Action
----  -------
T+0s  Start PostgreSQL, RabbitMQ, Redis
T+5s  Verify all infrastructure is ready
T+10s Cd to shared-library && mvn clean install
T+20s Cd to Eureka && mvn spring-boot:run
T+40s Eureka dashboard shows "0 instances" (no services yet)
T+45s Start 4 backend services in parallel (or sequential)
T+60s EmployeeService registered with Eureka ✓
T+60s DepartmentService registered with Eureka ✓
T+60s LeaveService registered with Eureka ✓
T+60s NotificationService registered with Eureka ✓
T+65s Start APIGateway
T+80s All 5 services + Eureka fully operational ✓
```

---

## 🎯 Service Startup Order (Summary Table)

| Order | Service | Port | Depends On | Registration |
|-------|---------|------|-----------|--------------|
| 1 | Infrastructure (DB, RabbitMQ, Redis) | Various | OS | N/A |
| 2 | Eureka | 8761 | Infrastructure | N/A |
| 3.1 | EmployeeService | 8081 | Eureka, PostgreSQL, RabbitMQ | ~30s after start |
| 3.2 | DepartmentService | 8082 | Eureka, PostgreSQL | ~30s after start |
| 3.3 | LeaveService | 8083 | Eureka, PostgreSQL, RabbitMQ | ~30s after start |
| 3.4 | NotificationService | 8084 | Eureka, PostgreSQL, RabbitMQ | ~30s after start |
| 4 | APIGateway | 8080 | All backend services | Immediate |

---

## ✅ Verification Checklist

After starting all services, verify:

```bash
# 1. Eureka Dashboard
curl http://localhost:8761/eureka/apps
# Should show: 5 applications (EMPLOYEE-SERVICE, DEPARTMENT-SERVICE, LEAVE-SERVICE, NOTIFICATION-SERVICE, API-GATEWAY)

# 2. Each Service Health
curl http://localhost:8081/actuator/health  # Employee
curl http://localhost:8082/actuator/health  # Department
curl http://localhost:8083/actuator/health  # Leave
curl http://localhost:8084/actuator/health  # Notification
curl http://localhost:8080/actuator/health  # Gateway

# All should return: {"status": "UP", ...}

# 3. Gateway is routing
curl http://localhost:8080/api/employees  # Proxies to EmployeeService

# 4. RabbitMQ queues are created
curl http://localhost:15672/api/queues
# Should show: leave.applied, leave.approved, leave.rejected, *.dlq queues
```

---

## 🆘 Common Startup Issues

### Issue 1: "Connection refused" for PostgreSQL
**Error**: `org.postgresql.util.PSQLException: Connection to localhost:5432 refused`

**Solution**:
```bash
# Check if PostgreSQL is running
psql --version
psql -U postgres -c "SELECT 1"

# Start PostgreSQL (platform-specific)
# macOS: brew services start postgresql
# Ubuntu: sudo service postgresql start
# Windows: Open Services and start PostgreSQL
```

---

### Issue 2: RabbitMQ Connection Failed
**Error**: `java.net.ConnectException: Connection refused`

**Solution**:
```bash
# Check if RabbitMQ is running
curl http://localhost:15672

# Start RabbitMQ
rabbitmq-server

# Or as service
# macOS: brew services start rabbitmq
# Ubuntu: sudo service rabbitmq-server start
```

---

### Issue 3: LeaveService starts but fails with queue error
**Error**: `Failed to declare queue(s):[leave.approved.dlq]`

**Solution**: 
Ensure `spring.rabbitmq.enabled=true` is set in `application.properties` for **all services**. This was fixed in previous update.

```bash
# Verify in each service's application.properties
grep "spring.rabbitmq.enabled" */src/main/resources/application.properties
```

---

### Issue 4: Redis Connection Failed
**Error**: `io.lettuce.core.RedisConnectionException`

**Solution**:
```bash
# Check if Redis is running
redis-cli ping
# Should return: PONG

# Start Redis
redis-server

# Or as service (macOS)
brew services start redis
```

---

### Issue 5: Service not registering in Eureka
**Error**: Service doesn't appear on Eureka dashboard after 2 minutes

**Solution**:
```bash
# 1. Verify Eureka is running
curl http://localhost:8761/eureka/apps

# 2. Check service is healthy
curl http://localhost:8081/actuator/health

# 3. Check application.properties has Eureka config
grep "eureka" */src/main/resources/application.properties

# 4. Wait 30-40 seconds (heartbeat interval)
# 5. Restart the service
```

---

### Issue 6: "Port already in use"
**Error**: `Address already in use`

**Solution**:
```bash
# Check what's using the port (Windows PowerShell)
Get-NetTcpConnection -LocalPort 8081

# Kill the process
Stop-Process -Id <PID> -Force

# Or use different port
# Add to application.properties: server.port=8081
```

---

## 🔧 Quick Startup Script

### Linux/macOS
```bash
#!/bin/bash
set -e

echo "🔄 Starting ELMS Infrastructure..."
echo "1. Ensure PostgreSQL is running"
echo "2. Ensure RabbitMQ is running (rabbitmq-server)"
echo "3. Ensure Redis is running (redis-server)"
echo ""
echo "Starting services..."

# Build shared library
echo "📦 Building shared-library..."
cd shared-library && mvn clean install && cd ..

# Start Eureka
echo "🔍 Starting Eureka (port 8761)..."
cd Eureka && mvn spring-boot:run &
EUREKA_PID=$!
sleep 20

# Start backend services
echo "🚀 Starting backend services..."
cd EmployeeService && mvn spring-boot:run &
cd ../DepartmentService && mvn spring-boot:run &
cd ../LeaveService && mvn spring-boot:run &
cd ../NotificationService && mvn spring-boot:run &
sleep 30

# Start Gateway
echo "🌐 Starting API Gateway (port 8080)..."
cd APIGateway && mvn spring-boot:run &

echo "✅ All services starting..."
echo "📊 Monitor at: http://localhost:8761"
echo "🔌 API Gateway: http://localhost:8080"
echo "🐰 RabbitMQ UI: http://localhost:15672"

wait
```

---

## 📌 Important Notes

1. **Build Order**: `shared-library` MUST be built first (mvn install)
2. **Eureka Registration**: Takes ~30 seconds per service
3. **Parallel Starting**: Backend services can start simultaneously (no inter-service dependencies)
4. **Infrastructure**: All infrastructure (DB, RabbitMQ, Redis) must be available before ANY service starts
5. **Gateway Last**: API Gateway should start after all backend services are registered
6. **Graceful Shutdown**: Use `Ctrl+C` in each terminal to gracefully shut down services

---

## 🎓 Why This Order?

### Eureka First (after infrastructure)
- All services register with Eureka
- Services discover each other via Eureka
- Without it, services won't find each other

### Backend Services Parallel
- No inter-service dependencies at startup
- They register independently with Eureka
- Reduces total startup time

### Gateway Last
- Needs all backend services to be registered
- If gateway starts before services, routing will fail
- Gateway acts as reverse proxy to all services

### Shared Library Build First
- All services depend on `elms-shared-library-1.0.0.jar`
- Must be in Maven local repository
- Contains common security, events, exceptions, etc.

---

## 🎯 Quick Reference: Service Ports

```
8761  - Eureka Server (Service Discovery)
8080  - API Gateway
8081  - EmployeeService
8082  - DepartmentService
8083  - LeaveService
8084  - NotificationService
5432  - PostgreSQL
5672  - RabbitMQ AMQP
15672 - RabbitMQ Management UI
6379  - Redis
```

---

## 📞 Testing After Startup

Once all services are running:

```bash
# Test through API Gateway
curl -X GET http://localhost:8080/api/employees

# Test RabbitMQ queues created
curl http://localhost:15672/api/queues

# Test Eureka discovery
curl http://localhost:8761/eureka/apps

# Test individual service health
for port in 8080 8081 8082 8083 8084 8761; do
  echo "Port $port:"
  curl http://localhost:$port/actuator/health
done
```

---

## ✨ Success Indicators

✅ **All services started successfully** if you see:
- All 5 terminals show "Started XXXApplication in XX seconds"
- Eureka shows 5 instances registered
- `curl http://localhost:8080/actuator/health` returns `{"status":"UP"}`
- No "Connection refused" errors in logs
- No "Failed to declare queue" errors for RabbitMQ

❌ **Something's wrong** if:
- Any service shows "ApplicationContextException" 
- Eureka shows 0 instances after 2 minutes
- RabbitMQ queue errors appear
- Database connection errors in logs

---

**Last Updated**: September 7, 2026  
**Status**: ✅ Ready for startup

