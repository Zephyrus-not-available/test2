# 🚀 Quick Start Guide
## Get Your Voting Application Running in Minutes

## Prerequisites

- ✅ Java 21+
- ✅ Maven 3.6+
- ✅ PostgreSQL 15+ (or Docker)

## 🎯 Fastest Way to Start (Docker)

```bash
# 1. Start everything with Docker Compose
docker-compose up -d

# 2. Wait 30 seconds for startup, then check health
curl http://localhost:8080/actuator/health

# 3. Access the application
# Open browser: http://localhost:8080
```

**That's it!** Your application is running.

## 📝 Manual Setup (Step by Step)

### Step 1: Database Setup

```bash
# Create database
createdb ktuvoting

# Run schema script
psql -d ktuvoting -f db.sql

# Create test PIN
psql -d ktuvoting -c "INSERT INTO voters (pin, device_id, has_voted) SELECT '12345', 'seed-' || EXTRACT(EPOCH FROM NOW())::BIGINT, FALSE WHERE NOT EXISTS (SELECT 1 FROM voters WHERE pin = '12345' LIMIT 1);"
```

### Step 2: Configure Database

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ktuvoting
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Step 3: Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/KTUVotingapp-*.jar

# Or with Maven
mvn spring-boot:run
```

### Step 4: Test

1. Open: `http://localhost:8080`
2. Enter PIN: `12345`
3. Complete voting flow
4. Check database to verify votes

## 🔍 Verify It's Working

```bash
# Check health
curl http://localhost:8080/actuator/health

# Check metrics
curl http://localhost:8080/actuator/metrics

# Check database (in psql)
SELECT * FROM voters WHERE pin = '12345';
SELECT * FROM votes;
```

## 📊 Test PIN

**PIN: `12345`**

This PIN works immediately if you ran the database setup SQL above.

## 🎯 Production Deployment

For production with 1500+ users, see:
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Complete guide
- **[PRODUCTION_SETUP.md](PRODUCTION_SETUP.md)** - Setup instructions

## ⚡ Performance Features

- ✅ Optimized for 1500+ concurrent users
- ✅ Connection pool: 150 connections
- ✅ Thread pool: 500 threads
- ✅ Caching: 5000 entries
- ✅ Monitoring: Actuator + Prometheus

## 🆘 Need Help?

- **Deployment**: See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- **Testing**: See [TESTING_GUIDE.md](TESTING_GUIDE.md)
- **Performance**: See [PERFORMANCE_OPTIMIZATION.md](PERFORMANCE_OPTIMIZATION.md)
- **Troubleshooting**: See [DEPLOYMENT_GUIDE.md#troubleshooting](DEPLOYMENT_GUIDE.md#troubleshooting)

## ✅ Quick Checklist

- [ ] Database created and configured
- [ ] Application started successfully
- [ ] Health check returns "UP"
- [ ] Can access http://localhost:8080
- [ ] Test PIN (12345) works
- [ ] Votes are being saved

**Ready to go!** 🎉

