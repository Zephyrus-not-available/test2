# Production Setup Guide

This guide will help you deploy the KTU Voting Application in a production environment.

## Prerequisites

1. **Java 21** or higher installed
2. **PostgreSQL 12+** database server
3. **Maven 3.6+** for building
4. Server with at least **2GB RAM** (recommended: 4GB+)

## Step 1: Database Setup

### Create PostgreSQL Database

```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE ktuvoting;
CREATE USER ktuvote_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE ktuvoting TO ktuvote_user;
\c ktuvoting
```

### Initialize Database Schema

Run the SQL script to create tables and seed initial data:

```bash
psql -U ktuvote_user -d ktuvoting -f db.sql
```

Or manually execute the contents of `db.sql` in your PostgreSQL client.

## Step 2: Configure Application Properties

### Update `application.properties`

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://YOUR_DB_HOST:5432/ktuvoting
spring.datasource.username=ktuvote_user
spring.datasource.password=your_secure_password

# For production, use 'validate' instead of 'update'
spring.jpa.hibernate.ddl-auto=validate

# Server Configuration
server.port=8080

# Admin PIN (change this!)
admin.pin=YOUR_SECURE_ADMIN_PIN
```

### Production Profile (`application-prod.properties`)

The production profile is already configured with:
- Database schema validation (no auto-updates)
- Optimized connection pool settings
- Security-focused error handling
- Reduced logging

## Step 3: Build the Application

```bash
# Clean and build
mvn clean package

# The JAR file will be created at:
# target/KTUVotingapp-1.0.0.jar (or similar)
```

## Step 4: Run the Application

### Option A: Direct Java Execution

```bash
java -jar target/KTUVotingapp-1.0.0.jar --spring.profiles.active=prod
```

### Option B: Using Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Option C: Docker (Recommended for Production)

```bash
# Build Docker image
docker build -t ktuvotingapp:latest .

# Run with docker-compose (includes PostgreSQL)
docker-compose up -d

# Or run standalone
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/ktuvoting \
  -e SPRING_DATASOURCE_USERNAME=ktuvote_user \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  ktuvotingapp:latest
```

## Step 5: Verify Installation

1. **Check Application Health**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Access the Application**
   - Open browser: `http://your-server-ip:8080`
   - You should see the landing page

3. **Test Voting Flow**
   - Enter a PIN (from your database)
   - Go through all selection pages
   - Click "Confirm" on the summary page
   - Verify votes are saved in database

4. **Check Admin Results**
   ```
   http://your-server-ip:8080/api/admin/results?adminPin=YOUR_ADMIN_PIN
   ```

## Step 6: Database Verification

Check if votes are being saved:

```sql
-- Check voters
SELECT * FROM voters;

-- Check votes
SELECT v.pin, v.device_id, v.has_voted, v.voted_at 
FROM voters v 
WHERE v.has_voted = true;

-- Check vote counts
SELECT c.category, c.candidate_number, c.name, c.vote_count 
FROM candidates c 
ORDER BY c.category, c.candidate_number;

-- Check individual votes
SELECT v.pin, vo.category, c.candidate_number, c.name 
FROM votes vo
JOIN voters v ON vo.voter_id = v.id
JOIN candidates c ON vo.candidate_id = c.id
ORDER BY v.voted_at DESC;
```

## Step 7: Production Checklist

### Security
- [ ] Change default admin PIN
- [ ] Use strong database passwords
- [ ] Enable HTTPS/SSL (use reverse proxy like Nginx)
- [ ] Configure firewall rules
- [ ] Set up proper CORS origins (if needed)

### Performance
- [ ] Configure connection pool size based on expected load
- [ ] Set up database connection monitoring
- [ ] Enable database query logging (for debugging)
- [ ] Configure JVM memory settings:
  ```bash
  java -Xms512m -Xmx2g -jar KTUVotingapp-1.0.0.jar
  ```

### Monitoring
- [ ] Set up application logging
- [ ] Monitor database connection pool
- [ ] Track vote submission rates
- [ ] Set up alerts for errors

### Backup
- [ ] Set up regular database backups
- [ ] Test backup restoration process
- [ ] Document backup procedures

## Step 8: Reverse Proxy Setup (Nginx)

For production, use Nginx as a reverse proxy:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Step 9: Systemd Service (Linux)

Create `/etc/systemd/system/ktuvotingapp.service`:

```ini
[Unit]
Description=KTU Voting Application
After=network.target postgresql.service

[Service]
Type=simple
User=your-user
WorkingDirectory=/path/to/application
ExecStart=/usr/bin/java -jar /path/to/KTUVotingapp-1.0.0.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable ktuvotingapp
sudo systemctl start ktuvotingapp
sudo systemctl status ktuvotingapp
```

## Troubleshooting

### Votes Not Saving

1. **Check Database Connection**
   ```bash
   # Test PostgreSQL connection
   psql -U ktuvote_user -d ktuvoting -h your-db-host
   ```

2. **Check Application Logs**
   ```bash
   # View logs
   tail -f logs/application.log
   # Or if using systemd
   journalctl -u ktuvotingapp -f
   ```

3. **Verify API Endpoint**
   ```bash
   # Test bulk vote endpoint
   curl -X POST http://localhost:8080/api/voting/bulk-vote \
     -H "Content-Type: application/json" \
     -d '{
       "pin": "12345",
       "deviceId": "test-device",
       "votes": [{"category": "KING", "candidateNumber": 1}]
     }'
   ```

4. **Check Browser Console**
   - Open browser developer tools (F12)
   - Check Console tab for JavaScript errors
   - Check Network tab for API call failures

### Common Issues

**Issue: "PIN not found" error**
- Solution: Ensure PIN exists in `voters` table
- Check: `SELECT * FROM voters WHERE pin = '12345';`

**Issue: "Candidate not found" error**
- Solution: Ensure candidates are seeded in database
- Check: `SELECT * FROM candidates;`

**Issue: "Device already voted" error**
- Solution: This is expected behavior - one device can only vote once
- To reset: `UPDATE voters SET has_voted = false WHERE device_id = 'device-id';`

**Issue: Database connection timeout**
- Solution: Check database is running and accessible
- Verify firewall rules allow connection
- Check connection pool settings

## Performance Tuning

### For High Traffic (1000+ concurrent users)

1. **Database Connection Pool**
   ```properties
   spring.datasource.hikari.maximum-pool-size=100
   spring.datasource.hikari.minimum-idle=20
   ```

2. **JVM Settings**
   ```bash
   java -Xms1g -Xmx4g \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -jar KTUVotingapp-1.0.0.jar
   ```

3. **Database Indexes** (already configured in db.sql)
   - Ensure indexes exist on frequently queried columns
   - Monitor slow queries

## Support

For issues or questions:
1. Check application logs
2. Check database logs
3. Review browser console errors
4. Verify database schema matches `db.sql`

## Security Notes

⚠️ **IMPORTANT**: Before going live:
- Change all default passwords
- Use environment variables for sensitive data
- Enable HTTPS
- Configure proper firewall rules
- Set up database backups
- Review and test all security settings

