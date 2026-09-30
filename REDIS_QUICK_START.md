# Redis Installation - Quick Reference Card

## ⚡ 30-SECOND INSTALLATION

### **Windows (Docker)** 🐳
```powershell
docker run -d -p 6379:6379 --name redis-elms redis:7-alpine
docker exec redis-elms redis-cli ping
# Expected: PONG ✓
```

### **macOS** 🍎
```bash
brew install redis
redis-server
# In another terminal:
redis-cli ping
# Expected: PONG ✓
```

### **Linux (Ubuntu/Debian)** 🐧
```bash
sudo apt-get update
sudo apt-get install redis-server -y
sudo service redis-server start
redis-cli ping
# Expected: PONG ✓
```

---

## ✅ VERIFY REDIS IS WORKING

```bash
redis-cli ping
# Should print: PONG

# Or set a value
redis-cli SET foo bar
redis-cli GET foo
# Should print: bar
```

---

## 🎯 CHOOSE YOUR INSTALLATION METHOD

### **Windows Users** 👇

**Best Option**: Docker (if Docker Desktop installed)
```powershell
docker run -d -p 6379:6379 --name redis-elms redis:7-alpine
```

**Alternative**: WSL2 + Ubuntu
```bash
# In WSL terminal
sudo apt-get update
sudo apt-get install redis-server
redis-server
```

**Alternative**: Chocolatey
```powershell
choco install redis-64
redis-server
```

---

### **macOS Users** 👇

**Only Option** (Homebrew):
```bash
brew install redis
redis-server
# Keep this terminal open while using Redis
```

**To auto-start on boot**:
```bash
brew services start redis
```

---

### **Linux Users** 👇

**Only Option** (apt-get):
```bash
sudo apt-get update
sudo apt-get install redis-server -y
sudo service redis-server start
```

**To auto-start on boot**:
```bash
sudo systemctl enable redis-server
```

---

## 🚀 USAGE

### Start Redis
```bash
# Docker
docker start redis-elms

# macOS (Homebrew)
brew services start redis

# Linux
sudo systemctl start redis-server

# Manual run
redis-server
```

### Stop Redis
```bash
# Docker
docker stop redis-elms

# macOS
brew services stop redis

# Linux
sudo systemctl stop redis-server

# Manual (Ctrl+C in terminal)
```

### Test Connection
```bash
redis-cli ping
# Should return: PONG
```

---

## 🔍 TROUBLESHOOTING

| Issue | Fix |
|-------|-----|
| Port 6379 in use | `redis-server --port 6380` |
| Connection refused | Check if redis-server is running |
| Command not found | Install redis-tools package |
| Windows: redis-cli not found | Use Docker: `docker exec redis-elms redis-cli` |

---

## ✨ YOU'RE DONE!

Once Redis is running (verified with `redis-cli ping` → `PONG`):

1. ✅ PostgreSQL running
2. ✅ RabbitMQ running  
3. ✅ **Redis running** ← You are here
4. → Next: Build and start ELMS services

**Continue with**: `STARTUP_CHECKLIST.md`

---

**Installation Time**: 2-10 minutes  
**Difficulty**: Easy  
**Status**: ✅ Ready to Proceed

