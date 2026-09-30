# ELMS Project - Applications Startup Analysis Summary

## 📊 EXECUTIVE SUMMARY

**Total Applications to Start**: 6 services + 3 infrastructure components  
**Startup Order**: Sequential (infrastructure) → Eureka → Backend Services (parallel) → Gateway  
**Total Time**: ~5 minutes  
**Complexity**: Medium (6 services, 3 infrastructure, 5 databases)

---

## 🎯 THE ANSWER: Which Applications to Start First?

### **PRIORITY 0: Infrastructure (Must run continuously)**
These MUST be running before starting ANY service:
1. **PostgreSQL** (port 5432) - Database
2. **RabbitMQ** (port 5672) - Message Broker
3. **Redis** (port 6379) - Cache

---

### **PRIORITY 1: Shared Library Build** (One-time only)
```bash
cd shared-library
mvn clean install
```
All services depend on this JAR - build it once before anything else.

---

### **PRIORITY 2: Eureka Server** (Service Discovery)
```bash
cd Eureka
mvn spring-boot:run
```
- **Port**: 8761
- **Why First**: All services register here
- **Wait**: ~20 seconds

---

### **PRIORITY 3: Backend Services** (Can start in parallel)
Start all 4 simultaneously in separate terminals:

**Terminal A - Employee Service**
```bash
cd EmployeeService && mvn spring-boot:run
```
- Port: 8081
- Database: employee_db
- Provides: Employee CRUD operations

**Terminal B - Department Service**
```bash
cd DepartmentService && mvn spring-boot:run
```
- Port: 8082
- Database: department_db
- Provides: Department CRUD operations

**Terminal C - Leave Service**
```bash
cd LeaveService && mvn spring-boot:run
```
- Port: 8083
- Database: leave_db
- Depends on: RabbitMQ (event publishing/listening)
- Provides: Leave management, event publishing

**Terminal D - Notification Service**
```bash
cd NotificationService && mvn spring-boot:run
```
- Port: 8084
- Database: notification_db
- Depends on: RabbitMQ (event listening)
- Provides: Email notifications via event consumption

---

### **PRIORITY 4: API Gateway** (Last - depends on all services)
```bash
cd APIGateway
mvn spring-boot:run
```
- **Port**: 8080
- **Why Last**: Needs all backend services registered in Eureka
- **Wait**: ~30 seconds after backend services

---

## 📋 STARTUP ORDER TABLE

| # | Component | Port | Type | Start Command | Depends On | Duration |
|---|-----------|------|------|---------------|-----------|----------|
| 0 | PostgreSQL | 5432 | Infrastructure | System startup | - | Ongoing |
| 0 | RabbitMQ | 5672/15672 | Infrastructure | System startup | - | Ongoing |
| 0 | Redis | 6379 | Infrastructure | System startup | - | Ongoing |
| 1 | shared-library | - | Build | `mvn clean install` | - | 30s |
| 2 | Eureka | 8761 | Service Registry | `mvn spring-boot:run` | Infrastructure | 20s |
| 3a | EmployeeService | 8081 | Service | `mvn spring-boot:run` | Eureka, PostgreSQL | 30s |
| 3b | DepartmentService | 8082 | Service | `mvn spring-boot:run` | Eureka, PostgreSQL | 30s |
| 3c | LeaveService | 8083 | Service | `mvn spring-boot:run` | Eureka, PostgreSQL, RabbitMQ | 30s |
| 3d | NotificationService | 8084 | Service | `mvn spring-boot:run` | Eureka, PostgreSQL, RabbitMQ | 30s |
| 4 | APIGateway | 8080 | Gateway | `mvn spring-boot:run` | All services in Eureka | 30s |

---

## 🔄 VISUAL STARTUP FLOW

```
Infrastructure (PostgreSQL, RabbitMQ, Redis)
         ↓
    [Running continuously]
         ↓
  shared-library (mvn install)
         ↓
  Eureka Server (8761)
         ↓
  ┌─────────────────────────────────────────┐
  │  PARALLEL: Start 4 Backend Services      │
  ├─────────────────────────────────────────┤
  │  ├─ Employee Service (8081)  [30s]      │
  │  ├─ Department Service (8082) [30s]     │
  │  ├─ Leave Service (8083)     [30s]      │
  │  └─ Notification Service (8084) [30s]   │
  │                                         │
  │  Wait for all 4 to show "Started"      │
  │  Wait 30 more seconds for Eureka       │
  │  registration                           │
  └─────────────────────────────────────────┘
         ↓
  API Gateway (8080) [30s]
         ↓
  ✅ ALL SERVICES RUNNING
```

---

## 🌟 CRITICAL SUCCESS FACTORS

### ✅ MUST DO FIRST
1. [x] Infrastructure running (PostgreSQL, RabbitMQ, Redis)
2. [x] Build shared-library with `mvn clean install`
3. [x] Start Eureka server alone
4. [x] Wait 20-30 seconds for Eureka to be ready

### ✅ MUST DO SECOND
5. [x] Start 4 backend services in parallel (separate terminals)
6. [x] Wait ~60 seconds for all to register in Eureka
7. [x] Verify on http://localhost:8761 that all 4 appear

### ✅ MUST DO LAST
8. [x] Start API Gateway only after backend services registered
9. [x] Wait ~30 seconds for Gateway to start
10. [x] Test with `curl http://localhost:8080/actuator/health`

---

## ⚠️ CRITICAL DEPENDENCIES

### Infrastructure Dependencies
- **All services** → PostgreSQL (each has own database)
- **LeaveService, NotificationService, EmployeeService** → RabbitMQ
- **APIGateway** → Redis (rate limiting)
- **All services** → Redis (caching)

### Service Dependencies
- **All backend services** → Eureka (service discovery)
- **APIGateway** → All backend services (via Eureka load balancer)
- **NotificationService** → LeaveService events (RabbitMQ)

### Build Dependencies
- **All services** → shared-library JAR (must build first)

---

## 📱 QUICK START REFERENCE

### 5-Minute Startup (Copy-Paste)

**Terminal 1 (Eureka):**
```bash
cd C:\Users\Dell\Documents\ELMS\Eureka
mvn spring-boot:run
```

**Terminal 2 (Employee):**
```bash
cd C:\Users\Dell\Documents\ELMS\EmployeeService
mvn clean install
mvn spring-boot:run
```

**Terminal 3 (Department):**
```bash
cd C:\Users\Dell\Documents\ELMS\DepartmentService
mvn clean install
mvn spring-boot:run
```

**Terminal 4 (Leave):**
```bash
cd C:\Users\Dell\Documents\ELMS\LeaveService
mvn clean install
mvn spring-boot:run
```

**Terminal 5 (Notification):**
```bash
cd C:\Users\Dell\Documents\ELMS\NotificationService
mvn clean install
mvn spring-boot:run
```

**Terminal 6 (Gateway) - Start after 60 seconds:**
```bash
cd C:\Users\Dell\Documents\ELMS\APIGateway
mvn clean install
mvn spring-boot:run
```

---

## 🎯 VERIFICATION AFTER STARTUP

**Check Eureka Dashboard:**
```
http://localhost:8761
Should show 5 instances:
✓ EMPLOYEE-SERVICE
✓ DEPARTMENT-SERVICE
✓ LEAVE-SERVICE
✓ NOTIFICATION-SERVICE
✓ API-GATEWAY
```

**Test Each Service:**
```bash
curl http://localhost:8081/actuator/health  # Employee
curl http://localhost:8082/actuator/health  # Department
curl http://localhost:8083/actuator/health  # Leave
curl http://localhost:8084/actuator/health  # Notification
curl http://localhost:8080/actuator/health  # Gateway
```

**All should return:**
```json
{"status":"UP", "components":{...}}
```

---

## 📊 SERVICE INVENTORY

| Service | Purpose | Port | Database | RabbitMQ | Notes |
|---------|---------|------|----------|----------|-------|
| Employee Service | Employee CRUD | 8081 | employee_db | Publishes events | Must start |
| Department Service | Department CRUD | 8082 | department_db | - | Must start |
| Leave Service | Leave management | 8083 | leave_db | Publishes/Listens | Event producer |
| Notification Service | Email notifications | 8084 | notification_db | Listens | Event consumer |
| API Gateway | Request routing | 8080 | elms_gateway | - | Reverse proxy |
| Eureka Server | Service discovery | 8761 | - | - | Service registry |

---

## 🔗 SERVICE RELATIONSHIPS

```
┌─────────────────────────────────────────────────────────┐
│         CLIENT REQUESTS                                 │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────▼───────────┐
         │   API GATEWAY (8080)  │
         │  ├─ Rate Limiting     │
         │  └─ Route to Services │
         └───────────┬───────────┘
                     │
    ┌────────────────┼────────────────┬──────────────┐
    │                │                │              │
┌───▼────┐      ┌────▼────┐      ┌───▼────┐   ┌────▼────┐
│Employee│      │Department│      │ Leave  │   │Notific. │
│Service │      │ Service  │      │Service │   │ Service │
└────────┘      └──────────┘      └───┬────┘   └─────────┘
                                       │
                                    RabbitMQ
                                   (events)
                                       │
                                   Events flow:
                                   leave.applied
                                   leave.approved
                                   leave.rejected

      ↑ All register with ↓
┌──────────────────────────────┐
│     EUREKA (8761)            │
│  Service Discovery           │
│  Instance Registry           │
└──────────────────────────────┘
```

---

## 📈 PERFORMANCE NOTES

- **Parallel Startup**: Backend services can start simultaneously (30 seconds total, not 120)
- **Registration Delay**: Each service takes ~30 seconds to register with Eureka
- **Gateway Dependency**: Must wait for backend service registration before starting Gateway
- **Total Time**: ~5 minutes from infrastructure running to all services operational

---

## 🚨 DO NOT MAKE THESE MISTAKES

❌ Start APIGateway before Eureka  
❌ Start APIGateway before backend services  
❌ Skip building shared-library  
❌ Start services before infrastructure (PostgreSQL, RabbitMQ, Redis)  
❌ Close Eureka terminal (it must stay running)  
❌ Start all services in one terminal (they'll interfere)

---

## ✨ SUCCESS CHECKLIST

After following the startup sequence:

- [x] Infrastructure running (PostgreSQL, RabbitMQ, Redis)
- [x] shared-library built and installed
- [x] Eureka dashboard shows "Instances currently registered with Eureka"
- [x] All 5 services show as registered in Eureka
- [x] All health endpoints return HTTP 200
- [x] API Gateway can route to backend services
- [x] No "Connection refused" errors
- [x] No "Failed to declare queue" errors
- [x] RabbitMQ queues created (leave.applied, leave.approved, leave.rejected)

---

## 📞 HELP & DOCUMENTATION

| Document | Purpose |
|----------|---------|
| **STARTUP_CHECKLIST.md** | Step-by-step checklist |
| **STARTUP_DIAGRAM.md** | Visual architecture & timeline |
| **STARTUP_ORDER.md** | Detailed analysis & troubleshooting |
| **QUICK_START_GUIDE.md** | Complete setup & testing |
| **IMPLEMENTATION_GUIDE.md** | Feature deep dives |

---

## 🎓 KEY CONCEPTS

- **Microservices**: 6 independent services, each with own database
- **Service Discovery**: Eureka registers and discovers services
- **Event-Driven**: RabbitMQ enables asynchronous communication
- **API Gateway**: Single entry point for all clients
- **Distributed**: Services can run on different machines

---

## 📝 QUICK DECISION TREE

**Q: Where do I start?**  
A: Infrastructure (PostgreSQL, RabbitMQ, Redis)

**Q: What's the first service to start?**  
A: Eureka Server (service discovery)

**Q: Can I start Employee Service and Leave Service at the same time?**  
A: Yes! Backend services are parallel

**Q: When do I start API Gateway?**  
A: After all backend services register in Eureka (~60 seconds total)

**Q: What if a service won't connect to the database?**  
A: Check PostgreSQL is running and the database exists

**Q: Why does Leave Service need RabbitMQ?**  
A: To publish events (leave.applied, leave.approved, leave.rejected)

**Q: Can I shut down Eureka after all services start?**  
A: No! Services need Eureka for continuous service discovery

---

## 🏁 FINAL SUMMARY

**Start these applications in this order:**

1. ✅ **Infrastructure** (PostgreSQL, RabbitMQ, Redis) - BACKGROUND
2. ✅ **shared-library** - BUILD ONLY (one-time)
3. ✅ **Eureka Server** (8761) - SERVICE REGISTRY
4. ✅ **4 Backend Services in Parallel** (8081, 8082, 8083, 8084)
5. ✅ **API Gateway** (8080) - REVERSE PROXY

**Total time**: ~5 minutes  
**Terminals needed**: 6 (one per service)  
**Result**: Fully functional microservices architecture!

---

**Last Updated**: September 7, 2026  
**Status**: ✅ Analysis Complete - Ready for Implementation  
**Next Step**: Follow `STARTUP_CHECKLIST.md`

