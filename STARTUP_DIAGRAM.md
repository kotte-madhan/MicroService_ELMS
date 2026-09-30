# ELMS Project - Visual Startup Architecture

## 🏗️ Service Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         CLIENT / LOAD BALANCER                           │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                    ┌────────────▼──────────────┐
                    │    API GATEWAY (8080)     │
                    │  ├─ Rate Limiting         │
                    │  ├─ Request Routing       │
                    │  └─ Response Aggregation  │
                    └────────────┬──────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┬──────────────┐
         │                       │                       │              │
    ┌────▼────────┐  ┌──────────▼───────┐  ┌───────────▼────┐  ┌──────▼────────┐
    │  EMPLOYEE    │  │  DEPARTMENT      │  │  LEAVE         │  │ NOTIFICATION   │
    │  SERVICE     │  │  SERVICE (8082)  │  │  SERVICE       │  │ SERVICE (8084) │
    │  (8081)      │  │                  │  │  (8083)        │  │                │
    │              │  │ ├─ Create Dept   │  │                │  │ ├─ Email       │
    │ ├─ Create    │  │ ├─ Get Dept      │  │ ├─ Apply Leave │  │ ├─ Send SMS    │
    │ ├─ List      │  │ ├─ Update Dept   │  │ ├─ Approve     │  │ ├─ Log Events  │
    │ ├─ Update    │  │ └─ Delete Dept   │  │ ├─ Reject      │  │ └─ Notify      │
    │ └─ Delete    │  └──────────────────┘  │ └─ Cancel      │  └────────────────┘
    └────┬─────────┘                         └───────┬────────┘
         │                                           │
         │   ┌─────────────────────────────────────┘
         │   │
         │   │  RabbitMQ Events
         │   │  ├─ leave.applied
         │   │  ├─ leave.approved
         │   │  ├─ leave.rejected
         │   │  └─ *.dlq (dead letter queues)
         │   │
         │   └────────────────────────────────────┐
         │                                         │
         │  ┌────────────────────────────────────▼─────────────────────┐
         │  │              EUREKA SERVICE DISCOVERY (8761)             │
         │  │  ├─ Employee Service Instance (8081)                     │
         │  │  ├─ Department Service Instance (8082)                   │
         │  │  ├─ Leave Service Instance (8083)                        │
         │  │  ├─ Notification Service Instance (8084)                 │
         │  │  └─ API Gateway Instance (8080)                          │
         │  └───────────────────────────────────────────────────────────┘
         │
         └──────────────────────────┐
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         │                          │                          │
    ┌────▼──────────┐  ┌────────────▼────────┐  ┌──────────────▼────┐
    │  POSTGRESQL   │  │   RABBITMQ          │  │    REDIS           │
    │  (5432)       │  │   (5672, 15672)     │  │    (6379)          │
    │               │  │                     │  │                    │
    │ ├─employee_db │  │ ├─ Exchanges        │  │ ├─ Rate Limit      │
    │ ├─dept_db     │  │ ├─ Queues           │  │ ├─ Idempotency     │
    │ ├─leave_db    │  │ ├─ Bindings         │  │ └─ Caching         │
    │ └─notify_db   │  │ └─ DLQ Handler      │  │                    │
    └───────────────┘  └─────────────────────┘  └────────────────────┘
```

---

## 🚀 STARTUP SEQUENCE

```
┌─────────────────────────────────────────────────────────────────────────┐
│ PHASE 0: INFRASTRUCTURE (Must be running FIRST)                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ☑ PostgreSQL (port 5432)                                               │
│  ☑ RabbitMQ (port 5672, management 15672)                               │
│  ☑ Redis (port 6379)                                                    │
│                                                                          │
│  Command: Start these on system startup or manually before services     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: BUILD SHARED LIBRARY (One-time only)                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  $ cd shared-library                                                    │
│  $ mvn clean install                                                    │
│                                                                          │
│  Output: Installs elms-shared-library-1.0.0.jar to ~/.m2/repository    │
│  Duration: ~30 seconds                                                  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ PHASE 2: START EUREKA SERVER (Gateway to all services)                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Terminal 1:                                                            │
│  $ cd Eureka                                                            │
│  $ mvn spring-boot:run                                                  │
│                                                                          │
│  ✓ Port: 8761                                                           │
│  ✓ Dashboard: http://localhost:8761                                     │
│  ✓ Status: "Eureka Server started" in console                           │
│  ✓ Wait Time: ~20 seconds                                               │
│                                                                          │
│  WHY FIRST?                                                             │
│  - All other services register with Eureka on startup                   │
│  - Without Eureka, services can't discover each other                   │
│  - Eureka is the service registry                                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ PHASE 3: START BACKEND SERVICES (Parallel - Open 4 terminals)           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  CAN START SIMULTANEOUSLY - No inter-service dependencies               │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Terminal 2 - EMPLOYEE SERVICE (Port 8081)                       │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ $ cd EmployeeService                                            │   │
│  │ $ mvn clean install                                             │   │
│  │ $ mvn spring-boot:run                                           │   │
│  │                                                                  │   │
│  │ ✓ Waits for: PostgreSQL, RabbitMQ                               │   │
│  │ ✓ Registers to: Eureka (after ~30 seconds)                      │   │
│  │ ✓ Provides: Employee CRUD operations                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Terminal 3 - DEPARTMENT SERVICE (Port 8082)                     │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ $ cd DepartmentService                                          │   │
│  │ $ mvn clean install                                             │   │
│  │ $ mvn spring-boot:run                                           │   │
│  │                                                                  │   │
│  │ ✓ Waits for: PostgreSQL                                         │   │
│  │ ✓ Registers to: Eureka (after ~30 seconds)                      │   │
│  │ ✓ Provides: Department CRUD operations                          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Terminal 4 - LEAVE SERVICE (Port 8083)                          │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ $ cd LeaveService                                               │   │
│  │ $ mvn clean install                                             │   │
│  │ $ mvn spring-boot:run                                           │   │
│  │                                                                  │   │
│  │ ✓ Waits for: PostgreSQL, RabbitMQ, Eureka                       │   │
│  │ ✓ Creates RabbitMQ queues: leave.applied, leave.approved, etc.  │   │
│  │ ✓ Registers to: Eureka (after ~30 seconds)                      │   │
│  │ ✓ Publishes: Leave events to RabbitMQ                           │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ Terminal 5 - NOTIFICATION SERVICE (Port 8084)                   │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │ $ cd NotificationService                                        │   │
│  │ $ mvn clean install                                             │   │
│  │ $ mvn spring-boot:run                                           │   │
│  │                                                                  │   │
│  │ ✓ Waits for: PostgreSQL, RabbitMQ, Eureka                       │   │
│  │ ✓ Listens to: leave.applied, leave.approved, leave.rejected     │   │
│  │ ✓ Registers to: Eureka (after ~30 seconds)                      │   │
│  │ ✓ Sends: Email notifications via RabbitMQ events               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│  TOTAL WAIT TIME: ~60 seconds (all services register with Eureka)       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ PHASE 4: START API GATEWAY (Last - depends on all services)             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Terminal 6:                                                            │
│  $ cd APIGateway                                                        │
│  $ mvn clean install                                                    │
│  $ mvn spring-boot:run                                                  │
│                                                                          │
│  ✓ Port: 8080                                                           │
│  ✓ Routes to: All backend services via Eureka discovery                 │
│  ✓ Status: "Started APIGateway in X seconds"                            │
│  ✓ Wait Time: ~30 seconds                                               │
│                                                                          │
│  WHY LAST?                                                              │
│  - Gateway needs all backend services registered in Eureka              │
│  - Gateway uses Eureka for load balancing and routing                   │
│  - If gateway starts too early, it can't route to services              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

```

---

## ⏱️ TIMELINE

```
Time (s)  Event
────────  ─────────────────────────────────────────────────────────────
0         Infrastructure verified (PostgreSQL, RabbitMQ, Redis running)
5         shared-library build started
35        shared-library build complete ✓
35        Eureka startup initiated
55        Eureka dashboard online @ http://localhost:8761 ✓
55        Backend services (4) startup initiated in parallel
60        EmployeeService registered in Eureka ✓
60        DepartmentService registered in Eureka ✓
60        LeaveService registered with RabbitMQ queues created ✓
60        NotificationService listening to RabbitMQ ✓
60        API Gateway startup initiated
80        API Gateway online @ http://localhost:8080 ✓
85        ALL SERVICES FULLY OPERATIONAL ✓

Total elapsed time: ~85 seconds from start of shared-library build
```

---

## 📊 DEPENDENCY MATRIX

```
                    PostgreSQL  RabbitMQ  Redis  Eureka  Other Services
Eureka              -           -         -      -       -
Employee Service    ✓           ✓         ✓      ✓       -
Department Service  ✓           -         ✓      ✓       -
Leave Service       ✓           ✓         ✓      ✓       -
Notification Svc    ✓           ✓         ✓      ✓       -
API Gateway         -           -         ✓      ✓       All above ✓
```

---

## ✅ VERIFICATION CHECKLIST

After all services are running:

```
☑ PostgreSQL running
  └─ Test: psql -U postgres -c "SELECT 1"

☑ RabbitMQ running
  └─ Test: curl http://localhost:15672
  └─ Queues created: http://localhost:15672/#/queues

☑ Redis running
  └─ Test: redis-cli ping → PONG

☑ Eureka Dashboard
  └─ Visit: http://localhost:8761
  └─ Shows: 5 instances (Employee, Dept, Leave, Notification, Gateway)

☑ Employee Service health
  └─ Test: curl http://localhost:8081/actuator/health

☑ Department Service health
  └─ Test: curl http://localhost:8082/actuator/health

☑ Leave Service health
  └─ Test: curl http://localhost:8083/actuator/health

☑ Notification Service health
  └─ Test: curl http://localhost:8084/actuator/health

☑ API Gateway health
  └─ Test: curl http://localhost:8080/actuator/health

☑ Gateway routing works
  └─ Test: curl http://localhost:8080/api/employees

✅ ALL GREEN → System is ready!
```

---

## 🎯 QUICK REFERENCE

### Minimum Startup (Sequential)
```bash
# Terminal 1: Eureka
cd Eureka && mvn spring-boot:run

# Terminal 2: Backend services (parallel)
cd EmployeeService && mvn spring-boot:run &
cd DepartmentService && mvn spring-boot:run &
cd LeaveService && mvn spring-boot:run &
cd NotificationService && mvn spring-boot:run &

# Terminal 3: Gateway
cd APIGateway && mvn spring-boot:run
```

### Service Ports Quick Reference
```
8080  ← API Gateway (entry point)
8081  ← Employee Service
8082  ← Department Service
8083  ← Leave Service
8084  ← Notification Service
8761  ← Eureka Dashboard
15672 ← RabbitMQ Management UI
```

### Port Aliases (for convenience)
```bash
export GATEWAY=http://localhost:8080
export EUREKA=http://localhost:8761
export RABBITMQ=http://localhost:15672
```

---

## 🚨 CRITICAL POINTS

1. **Infrastructure FIRST** - DB, RabbitMQ, Redis must be running
2. **Eureka SECOND** - All services depend on service discovery
3. **Backend Services THIRD** - Can run in parallel
4. **Gateway LAST** - Needs all services registered
5. **Build shared-library** - Before building any service
6. **Wait for registration** - ~30 seconds for each service to appear in Eureka
7. **Check logs** - For any "Connection refused" errors

---

**Last Updated**: September 7, 2026  
**Version**: 1.0.0  
**Status**: ✅ Ready for Production Startup

