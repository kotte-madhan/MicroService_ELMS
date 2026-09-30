# Redis Installation Guide - Step by Step

## 🎯 QUICK SUMMARY

| OS | Method | Time | Difficulty |
|----|--------|------|-----------|
| **Windows** | Docker or WSL | 5-10 min | Easy |
| **macOS** | Homebrew | 2-3 min | Very Easy |
| **Linux (Ubuntu/Debian)** | apt-get | 2-3 min | Very Easy |

---

## 🪟 **WINDOWS INSTALLATION**

### **Option 1: Docker (Easiest - Recommended)** ⭐

**Prerequisites**: Docker Desktop installed

**Steps**:

1. **Open PowerShell or Command Prompt**

2. **Pull and Run Redis container**:
```powershell
docker run -d -p 6379:6379 --name redis-elms redis:7-alpine
```

3. **Verify it's running**:
```powershell
docker exec redis-elms redis-cli ping
# Expected output: PONG
```

4. **Check container status**:
```powershell
docker ps
# Should show: redis-elms container running
```

**✅ Done!** Redis is running on port 6379

---

### **Option 2: Windows Subsystem for Linux (WSL2)** (Intermediate)

**Prerequisites**: WSL2 installed and Ubuntu/Debian Linux distribution

**Steps**:

1. **Open WSL terminal** (search for Ubuntu in Windows Search)

2. **Update package manager**:
```bash
sudo apt-get update
sudo apt-get upgrade
```

3. **Install Redis**:
```bash
sudo apt-get install redis-server -y
```

4. **Start Redis service**:
```bash
sudo service redis-server start
```

5. **Verify it's running**:
```bash
redis-cli ping
# Expected: PONG
```

**✅ Done!** Redis is running in WSL

---

### **Option 3: Chocolatey Package Manager** (If Chocolatey installed)

**Prerequisites**: Chocolatey installed

**Steps**:

1. **Open PowerShell as Administrator**

2. **Install Redis**:
```powershell
choco install redis-64
```

3. **Start Redis**:
```powershell
redis-server
# Window will stay open - Redis running
```

4. **In another PowerShell window, verify**:
```powershell
redis-cli ping
# Expected: PONG
```

**✅ Done!** Redis is running

---

### **Option 4: Manual Download (Advanced)**

**Steps**:

1. **Download Redis for Windows**:
   - Visit: https://github.com/microsoftarchive/redis/releases
   - Download: `Redis-x64-x.x.x.msi`

2. **Run installer**
   - Double-click `.msi` file
   - Click "Next" through wizard
   - Accept default installation path
   - Finish installation

3. **Verify installation**:
   - Open PowerShell
   - Type: `redis-cli ping`
   - Expected: PONG

**✅ Done!**

---

## 🍎 **macOS INSTALLATION**

### **Option 1: Homebrew (Easiest - Recommended)** ⭐

**Prerequisites**: Homebrew installed (install from https://brew.sh)

**Steps**:

1. **Open Terminal**

2. **Install Redis**:
```bash
brew install redis
```

3. **Start Redis as a service** (optional):
```bash
brew services start redis
```

4. **Or manually start**:
```bash
redis-server
```

5. **Verify it's running** (in another Terminal):
```bash
redis-cli ping
# Expected: PONG
```

**✅ Done!** Redis is running

---

### **Option 2: Docker** (If Docker Desktop installed)

```bash
docker run -d -p 6379:6379 --name redis-elms redis:7-alpine

# Verify
docker exec redis-elms redis-cli ping
# Expected: PONG
```

**✅ Done!**

---

### **Option 3: Compile from Source** (Advanced)

```bash
# Download and compile
curl -O http://download.redis.io/redis-stable.tar.gz
tar -xzf redis-stable.tar.gz
cd redis-stable
make
sudo make install

# Run
redis-server

# Verify (new terminal)
redis-cli ping
```

**✅ Done!**

---

## 🐧 **LINUX (Ubuntu/Debian) INSTALLATION**

### **Option 1: apt-get (Easiest - Recommended)** ⭐

**Steps**:

1. **Open Terminal**

2. **Update package lists**:
```bash
sudo apt-get update
```

3. **Install Redis**:
```bash
sudo apt-get install redis-server -y
```

4. **Start Redis service**:
```bash
sudo service redis-server start
```

5. **Verify it's running**:
```bash
redis-cli ping
# Expected: PONG
```

**Optional - Auto-start on boot**:
```bash
sudo systemctl enable redis-server
```

**✅ Done!** Redis is running

---

### **Option 2: Docker** (If Docker installed)

```bash
docker run -d -p 6379:6379 --name redis-elms redis:7-alpine

# Verify
docker exec redis-elms redis-cli ping
# Expected: PONG
```

**✅ Done!**

---

### **Option 3: Compile from Source** (Advanced)

```bash
# Install dependencies
sudo apt-get install build-essential tcl

# Download and compile
curl -O http://download.redis.io/redis-stable.tar.gz
tar -xzf redis-stable.tar.gz
cd redis-stable
make
sudo make install

# Run
redis-server

# Verify (new terminal)
redis-cli ping
```

**✅ Done!**

---

## ✅ VERIFICATION (All Platforms)

### **Test 1: Ping Redis**
```bash
redis-cli ping
# Expected output: PONG
```

### **Test 2: Set and Get a value**
```bash
redis-cli
> SET mykey "Hello"
OK
> GET mykey
"Hello"
> EXIT
```

### **Test 3: Check in Java application**
```bash
# Start a service and check actuator
curl http://localhost:8081/actuator/health
# Should show redis component as UP (if configured)
```

---

## 🚀 KEEPING REDIS RUNNING

### **Windows (Docker)**
```powershell
# Stop
docker stop redis-elms

# Start again
docker start redis-elms

# View logs
docker logs redis-elms
```

### **macOS (Homebrew Service)**
```bash
# Start
brew services start redis

# Stop
brew services stop redis

# Restart
brew services restart redis

# View status
brew services list
```

### **Linux (systemctl)**
```bash
# Start
sudo systemctl start redis-server

# Stop
sudo systemctl stop redis-server

# Restart
sudo systemctl restart redis-server

# Status
sudo systemctl status redis-server
```

---

## 🔧 COMMON ISSUES & SOLUTIONS

### Issue 1: "redis-cli: command not found"

**Windows Solution**:
```powershell
# If using WSL, make sure you're in WSL terminal
# If using Docker, use:
docker exec redis-elms redis-cli ping
```

**macOS/Linux Solution**:
```bash
# Install redis-tools
sudo apt-get install redis-tools  # Ubuntu/Debian
brew install redis                # macOS
```

---

### Issue 2: "Port 6379 already in use"

```bash
# Find what's using port 6379
# Windows PowerShell:
Get-NetTcpConnection -LocalPort 6379

# macOS/Linux:
lsof -i :6379

# Kill the process
# Windows:
Stop-Process -Id <PID> -Force

# macOS/Linux:
sudo kill -9 <PID>

# Or use different port
redis-server --port 6380
```

---

### Issue 3: "Connection refused"

**Solutions**:
1. Check Redis is running: `redis-cli ping`
2. Check correct port: Default is 6379
3. Check firewall isn't blocking port 6379
4. Restart Redis service:
   ```bash
   # Docker
   docker restart redis-elms
   
   # macOS
   brew services restart redis
   
   # Linux
   sudo systemctl restart redis-server
   ```

---

### Issue 4: Redis starts but services can't connect

**Cause**: Redis only listening on localhost (default)

**Solution**: Check Redis configuration allows connections from your application

```bash
# Test connection from Java app
curl http://localhost:8081/actuator/health
# If redis shows DOWN, check Redis is accessible
```

---

## 📋 POST-INSTALLATION CHECKLIST

After installing Redis, verify:

- [x] `redis-cli ping` returns `PONG`
- [x] Redis accessible on `localhost:6379`
- [x] Redis starts automatically (or via service command)
- [x] Can set/get values:
  ```bash
  redis-cli SET test "works"
  redis-cli GET test
  ```
- [x] Rate limiting will work in API Gateway
- [x] Services can connect to Redis

---

## 🎯 RECOMMENDED INSTALLATION

| OS | Recommendation | Command |
|----|-----------------|---------|
| **Windows 10/11** | Docker | `docker run -d -p 6379:6379 --name redis-elms redis:7-alpine` |
| **macOS** | Homebrew | `brew install redis && brew services start redis` |
| **Linux** | apt-get | `sudo apt-get install redis-server && sudo systemctl start redis-server` |

---

## 🚀 NEXT STEPS AFTER REDIS INSTALLATION

1. ✅ Verify Redis is running: `redis-cli ping` → `PONG`
2. ✅ Start infrastructure (PostgreSQL, RabbitMQ already done)
3. ✅ Build shared-library
4. ✅ Start Eureka Server
5. ✅ Start backend services
6. ✅ Start API Gateway

**Now all 3 infrastructure components are ready!**

---

## 📞 HELP

**Stuck?** Common issues:

```bash
# Still getting connection refused?
redis-server --version              # Check if installed

# Redis running but services can't find it?
redis-cli -h localhost -p 6379      # Test connection

# Need to check Redis data?
redis-cli
> DBSIZE                            # Number of keys
> KEYS *                            # List all keys
> FLUSHDB                           # Clear database
> EXIT
```

---

**Status**: ✅ Ready to Install Redis  
**Time to Complete**: 5-10 minutes  
**Difficulty**: Easy  
**Next**: Return to `STARTUP_CHECKLIST.md` and continue


