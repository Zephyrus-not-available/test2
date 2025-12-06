# Production Deployment Guide
## Optimized for 1500+ Concurrent Users

This guide provides comprehensive instructions for deploying the KTU Voting Application in a production environment optimized for high concurrency.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [System Requirements](#system-requirements)
3. [Database Setup](#database-setup)
4. [Application Configuration](#application-configuration)
5. [Build & Deploy](#build--deploy)
6. [Performance Optimization](#performance-optimization)
7. [Monitoring & Health Checks](#monitoring--health-checks)
8. [Load Balancing](#load-balancing)
9. [Scaling](#scaling)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **PostgreSQL 12+** (recommended: PostgreSQL 15)
- **Linux server** (Ubuntu 20.04+, CentOS 8+, or similar)
- **Minimum 4GB RAM** (8GB+ recommended for 1500+ users)
- **Root/sudo access** for system configuration

## System Requirements

### For 1500+ Concurrent Users:

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| CPU | 2 cores | 4+ cores |
| RAM | 4GB | 8GB+ |
| Disk | 20GB SSD | 50GB+ SSD |
| Network | 100 Mbps | 1 Gbps |
| Database RAM | 2GB | 4GB+ |

### PostgreSQL Configuration:

```sql
-- PostgreSQL settings for high concurrency
max_connections = 200
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 4MB
min_wal_size = 1GB
max_wal_size = 4GB
```

## Database Setup

### 1. Install PostgreSQL

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql-15 postgresql-contrib-15

# CentOS/RHEL
sudo yum install postgresql15-server postgresql15-contrib
```

### 2. Initialize Database

```bash
# Create database and user
sudo -u postgres psql << EOF
CREATE DATABASE ktuvoting;
CREATE USER ktuvote_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE ktuvoting TO ktuvote_user;
\c ktuvoting
GRANT ALL ON SCHEMA public TO ktuvote_user;
EOF
```

### 3. Run Database Schema Script

```bash
psql -U ktuvote_user -d ktuvoting -f db.sql
```

### 4. Optimize PostgreSQL Configuration

Edit `/etc/postgresql/15/main/postgresql.conf`:

```conf
max_connections = 200
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB
```

Restart PostgreSQL:
```bash
sudo systemctl restart postgresql
```

## Application Configuration

### 1. Update Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ktuvoting
spring.datasource.username=ktuvote_user
spring.datasource.password=your_secure_password
```

### 2. Environment Variables

Create `.env` file or set environment variables:

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ktuvoting
export SPRING_DATASOURCE_USERNAME=ktuvote_user
export SPRING_DATASOURCE_PASSWORD=your_secure_password
export ADMIN_PIN=your_admin_pin
```

## Build & Deploy

### Option 1: Standalone Deployment

```bash
# Build the application
mvn clean package -DskipTests

# Make scripts executable
chmod +x scripts/*.sh

# Start the application
./scripts/start-production.sh

# Check logs
tail -f logs/application.log
```

### Option 2: Docker Deployment

```bash
# Build and start with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f ktuvotingapp

# Stop
docker-compose down
```

### Option 3: Systemd Service (Recommended)

Create `/etc/systemd/system/ktuvotingapp.service`:

```ini
[Unit]
Description=KTU Voting Application
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=ktuvote
Group=ktuvote
WorkingDirectory=/opt/ktuvotingapp
ExecStart=/usr/bin/java -XX:+UseG1GC -Xms2g -Xmx4g -XX:MaxGCPauseMillis=200 -jar /opt/ktuvotingapp/app.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=ktuvotingapp

# Security
NoNewPrivileges=true
PrivateTmp=true

# Resource limits
LimitNOFILE=65536
LimitNPROC=4096

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl enable ktuvotingapp
sudo systemctl start ktuvotingapp
sudo systemctl status ktuvotingapp
```

## Performance Optimization

### JVM Settings

For **4GB RAM**:
```bash
-Xms1g -Xmx2g
```

For **8GB RAM**:
```bash
-Xms2g -Xmx4g
```

For **16GB+ RAM**:
```bash
-Xms4g -Xmx8g
```

### Connection Pool

Already optimized in `application.properties`:
- Maximum Pool Size: 150 connections
- Minimum Idle: 30 connections
- Connection Timeout: 30 seconds

### Thread Pool

Already optimized:
- Max Threads: 500
- Min Spare Threads: 50
- Max Connections: 10,000

## Monitoring & Health Checks

### Health Endpoints

- **Application Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:9090/actuator/prometheus`

### Key Metrics to Monitor

1. **Response Time** (p95 < 500ms)
2. **Throughput** (> 3000 req/min)
3. **Connection Pool Usage** (< 80%)
4. **Error Rate** (< 0.1%)
5. **GC Pause Time** (< 200ms)
6. **Database Connection Count**

### Monitoring Commands

```bash
# Check application status
curl http://localhost:8080/actuator/health

# Check metrics
curl http://localhost:8080/actuator/metrics

# Check database connections
curl http://localhost:8080/actuator/metrics/hikari.connections.active

# View application logs
tail -f logs/application.log
```

## Load Balancing

### Nginx Configuration

Create `/etc/nginx/sites-available/ktuvoting`:

```nginx
upstream ktuvoting_backend {
    least_conn;
    server localhost:8080 max_fails=3 fail_timeout=30s;
    # Add more servers for horizontal scaling
    # server localhost:8081 max_fails=3 fail_timeout=30s;
    # server localhost:8082 max_fails=3 fail_timeout=30s;
    keepalive 32;
}

server {
    listen 80;
    server_name your-domain.com;

    client_max_body_size 1M;
    client_body_timeout 10s;

    location / {
        proxy_pass http://ktuvoting_backend;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }

    location /actuator {
        # Restrict access to monitoring endpoints
        allow 127.0.0.1;
        deny all;
        proxy_pass http://ktuvoting_backend;
    }
}
```

Enable and restart:
```bash
sudo ln -s /etc/nginx/sites-available/ktuvoting /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## Scaling

### Vertical Scaling (Single Instance)

Increase server resources:
- More CPU cores
- More RAM
- Faster disk (SSD)

Update JVM settings accordingly.

### Horizontal Scaling (Multiple Instances)

1. **Deploy multiple instances** on different ports:
   - Instance 1: Port 8080
   - Instance 2: Port 8081
   - Instance 3: Port 8082

2. **Use load balancer** (Nginx) to distribute traffic

3. **Shared database** - all instances connect to same PostgreSQL

4. **Session stickiness** - not required (stateless application)

### Database Scaling

For very high load (5000+ users):

1. **Read Replicas**: Configure PostgreSQL read replicas
2. **Connection Pooling**: Use PgBouncer for connection pooling
3. **Database Clustering**: Consider PostgreSQL clustering solutions

## Troubleshooting

### High Memory Usage

```bash
# Check JVM heap usage
jmap -heap <PID>

# Generate heap dump
jmap -dump:format=b,file=heapdump.hprof <PID>
```

### Connection Pool Exhausted

1. Check database connections:
```sql
SELECT count(*) FROM pg_stat_activity WHERE datname = 'ktuvoting';
```

2. Check HikariCP metrics:
```bash
curl http://localhost:8080/actuator/metrics/hikari.connections.active
```

3. Increase pool size if needed (max 200 for PostgreSQL default)

### Slow Queries

Enable slow query logging:

```properties
spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=1000
```

Check PostgreSQL slow queries:

```sql
SELECT query, mean_exec_time, calls 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;
```

### Application Not Starting

1. Check logs: `tail -f logs/application.log`
2. Check database connection
3. Check port availability: `netstat -tulpn | grep 8080`
4. Check Java version: `java -version`

## Performance Benchmarks

### Expected Performance (1500 concurrent users):

- **Response Time (p95)**: < 500ms
- **Throughput**: > 3000 requests/minute
- **Error Rate**: < 0.1%
- **Connection Pool Usage**: < 80%
- **CPU Usage**: < 70%
- **Memory Usage**: < 80%

### Load Testing

Use Apache Bench or JMeter:

```bash
# Install Apache Bench
sudo apt install apache2-utils

# Load test
ab -n 10000 -c 100 -p vote_request.json -T application/json \
   http://localhost:8080/api/voting/bulk-vote
```

## Security Checklist

- [ ] Change default admin PIN
- [ ] Use strong database passwords
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Restrict actuator endpoints
- [ ] Enable database SSL connections
- [ ] Set up regular backups
- [ ] Monitor logs for suspicious activity

## Backup Strategy

### Database Backup

```bash
# Daily backup script
#!/bin/bash
BACKUP_DIR="/backups/ktuvoting"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -U ktuvote_user ktuvoting > "$BACKUP_DIR/backup_$DATE.sql"
```

### Automated Backups (Cron)

```bash
# Add to crontab
0 2 * * * /path/to/backup-script.sh
```

## Support & Maintenance

- **Log Location**: `logs/application.log`
- **PID File**: `app.pid`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:9090/actuator/prometheus`

For issues, check:
1. Application logs
2. System logs: `journalctl -u ktuvotingapp`
3. Database logs
4. Nginx logs (if using load balancer)

