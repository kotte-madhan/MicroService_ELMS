# ELMS Microservices - QUICK STARTUP CHECKLIST

## 🎯 START HERE - Step-by-Step Guide

---

## ✅ PRE-STARTUP CHECKS (5 minutes)

### ☐ Infrastructure Running?

```bash
# 1. PostgreSQL
psql -U postgres -c "SELECT 1"
# ✓ Expected: No error, SQL shell responds

# 2. RabbitMQ
curl http://localhost:15672
# ✓ Expected: HTML response (RabbitMQ dashboard loads)

# 3. Redis
redis-cli ping
# ✓ Expected: PONG
```

**❌ If any fails**: Start that infrastructure service first!

---

## 🏗️ PHASE 1: BUILD SHARED LIBRARY (Do once)

```bash
☐ cd C:\Users\Dell\Documents\ELMS\shared-library
☐ mvn clean install

📋 Expected output:
   - BUILD SUCCESS
   - JAR installed in ~/.m2/repository
```

⏱️ **Duration**: ~30 seconds

---

## 🔍 PHASE 2: START EUREKA SERVER

**Open Terminal 1:**

```bash
☐ cd C:\Users\Dell\Documents\ELMS\Eureka
☐ mvn spring-boot:run

📋 Expected output:
   - "Started EurekaServerApplication"
   - "Tomcat started on port 8761"
   - Dashboard at http://localhost:8761

🔗 Verify: http://localhost:8761
```

⏱️ **Duration**: ~20 seconds  
**Keep this terminal open**

---

## 🚀 PHASE 3: START 4 BACKEND SERVICES (Parallel)

Open **4 more terminals** and run simultaneously:

---

### **Terminal 2: EmployeeService**

```bash
☐ cd C:\Users\Dell\Documents\ELMS\EmployeeService
☐ mvn clean install
☐ mvn spring-boot:run

📋 Expected output:
   - "Started EmployeeServiceApplication"
   - "Tomcat started on port 8081"
   - "DiscoveryClient initialized"

🔗 Verify: http://localhost:8081/actuator/health
```

⏱️ **Duration**: ~30 seconds for build + start

---

### **Terminal 3: DepartmentService**

```bash
☐ cd C:\Users\Dell\Documents\ELMS\DepartmentService
☐ mvn clean install
☐ mvn spring-boot:run

📋 Expected output:
   - "Started DepartmentService"
   - "Tomcat started on port 8082"
   - "DiscoveryClient initialized"

🔗 Verify: http://localhost:8082/actuator/health
```

⏱️ **Duration**: ~30 seconds for build + start

---

### **Terminal 4: LeaveService**

```bash
☐ cd C:\Users\Dell\Documents\ELMS\LeaveService
☐ mvn clean install
☐ mvn spring-boot:run

📋 Expected output:
   - "Started LeaveServiceApplication"
   - "Tomcat started on port 8083"
   - "DiscoveryClient initialized"
   - "Created new connection: amqp://guest@127.0.0.1:5672"

🔗 Verify: http://localhost:8083/actuator/health
```

⏱️ **Duration**: ~30 seconds for build + start

---

### **Terminal 5: NotificationService**

```bash
☐ cd C:\Users\Dell\Documents\ELMS\NotificationService
☐ mvn clean install
☐ mvn spring-boot:run

📋 Expected output:
   - "Started NotificationServiceApplication"
   - "Tomcat started on port 8084"
   - "DiscoveryClient initialized"
   - "Created new connection: amqp://guest@127.0.0.1:5672"

🔗 Verify: http://localhost:8084/actuator/health
```

⏱️ **Duration**: ~30 seconds for build + start

---

## ⏳ WAIT FOR REGISTRATION (30 seconds)

**Check Eureka Dashboard:**

```bash
☐ Open browser: http://localhost:8761
☐ Wait until you see 4 services:
   ✓ EMPLOYEE-SERVICE (1 instance)
   ✓ DEPARTMENT-SERVICE (1 instance)
   ✓ LEAVE-SERVICE (1 instance)
   ✓ NOTIFICATION-SERVICE (1 instance)

This takes ~30 seconds after each service starts
```

---

## 🌐 PHASE 4: START API GATEWAY

**Open Terminal 6:**

```bash
☐ cd C:\Users\Dell\Documents\ELMS\APIGateway
☐ mvn clean install
☐ mvn spring-boot:run

📋 Expected output:
   - "Started APIGateway"
   - "Tomcat started on port 8080"
   - "DiscoveryClient initialized"

🔗 Verify: http://localhost:8080/actuator/health
```

⏱️ **Duration**: ~30 seconds for build + start

---

## ✅ FINAL VERIFICATION (All should show UP)

```bash
☐ Eureka Dashboard
   http://localhost:8761
   Should show: 5 instances

☐ Employee Service
   curl http://localhost:8081/actuator/health
   Response: {"status":"UP",...}

☐ Department Service
   curl http://localhost:8082/actuator/health
   Response: {"status":"UP",...}

☐ Leave Service
   curl http://localhost:8083/actuator/health
   Response: {"status":"UP",...}

☐ Notification Service
   curl http://localhost:8084/actuator/health
   Response: {"status":"UP",...}

☐ API Gateway
   curl http://localhost:8080/actuator/health
   Response: {"status":"UP",...}

☐ Gateway can route to services
   curl http://localhost:8080/api/employees
   Should return employee data (or empty list if no data)
```

---

## 🎉 SUCCESS INDICATORS

✅ You're done if you see:

- [x] All 6 terminals show service started successfully
- [x] No "Connection refused" errors in any terminal
- [x] No "Failed to declare queue" errors
- [x] Eureka shows 5 instances registered
- [x] All health check endpoints return HTTP 200 with status UP
- [x] API Gateway can route requests

---

## 🚨 TROUBLESHOOTING (If something fails)

| Issue | Solution |
|-------|----------|
| PostgreSQL error | Check if PostgreSQL running: `psql -U postgres -c "SELECT 1"` |
| RabbitMQ error | Check if RabbitMQ running: `curl http://localhost:15672` |
| Redis error | Check if Redis running: `redis-cli ping` (should return PONG) |
| Port in use | Kill process using the port or change port in application.properties |
| Service won't register | Wait 30 seconds, check Eureka logs, verify eureka.client.service-url.defaultZone |
| Queue error | Verify `spring.rabbitmq.enabled=true` in application.properties |
| Build fails | Clear Maven cache: `mvn clean -DskipTests=true` |

---

## 📋 TERMINAL ORGANIZATION

```
┌─ Terminal 1: Eureka Server (8761)
├─ Terminal 2: Employee Service (8081)
├─ Terminal 3: Department Service (8082)
├─ Terminal 4: Leave Service (8083)
├─ Terminal 5: Notification Service (8084)
└─ Terminal 6: API Gateway (8080)
```

**Keep all 6 terminals open while working!**

---

## 🔗 QUICK LINKS

| Service | URL | Type |
|---------|-----|------|
| Eureka Dashboard | http://localhost:8761 | UI |
| Employee Service | http://localhost:8081 | API |
| Department Service | http://localhost:8082 | API |
| Leave Service | http://localhost:8083 | API |
| Notification Service | http://localhost:8084 | API |
| API Gateway | http://localhost:8080 | API Gateway |
| RabbitMQ UI | http://localhost:15672 | UI (guest/guest) |

---

## 🧪 QUICK TEST

Once everything is running:

```bash
# Test API Gateway can reach Employee Service
curl -X GET http://localhost:8080/api/employees

# Test RabbitMQ created queues
curl http://localhost:15672/api/queues

# Test individual services
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

---

## 📊 ESTIMATED TIMING

| Phase | Duration | Cumulative |
|-------|----------|-----------|
| Build shared-library | 30s | 30s |
| Start Eureka | 20s | 50s |
| Build 4 backends | 30s each | 170s |
| Start 4 backends | 10s each | 210s |
| Wait for registration | 30s | 240s |
| Build Gateway | 25s | 265s |
| Start Gateway | 10s | 275s |
| **TOTAL** | **~5 minutes** | |

---

## 🎯 COMMON MISTAKES TO AVOID

❌ **Don't:**
- Start APIGateway before services are registered in Eureka
- Run services before infrastructure (DB, RabbitMQ, Redis) is ready
- Forget to build shared-library first
- Close terminal prematurely (services will stop)
- Try to start all services in one terminal (use separate terminals)

✅ **Do:**
- Verify infrastructure is running first
- Build shared-library exactly once
- Start Eureka before backend services
- Start backend services in parallel
- Start Gateway last
- Keep all terminals open
- Wait for "Started XXX in X seconds" message

---

## 📞 NEED HELP?

**If services won't start:**

```bash
# Check infrastructure
psql -U postgres -c "SELECT 1"          # PostgreSQL
curl http://localhost:15672             # RabbitMQ
redis-cli ping                           # Redis

# Check if ports are in use
netstat -tulpn | grep 8080              # Linux
Get-NetTcpConnection -LocalPort 8080    # Windows PowerShell

# View detailed logs (follow mode)
mvn spring-boot:run | head -100          # First 100 lines
```

**Detailed troubleshooting**: See `QUICK_START_GUIDE.md` or `STARTUP_ORDER.md`

---

## ✨ YOU'RE READY!

Follow this checklist in order, and your ELMS microservices will be running in ~5 minutes.

**Last Updated**: September 7, 2026


