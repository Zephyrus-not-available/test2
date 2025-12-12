# Render Deployment Guide - KTU Voting App

This guide will walk you through deploying the KTU Voting Application to Render.

## 🚀 Quick Deployment Steps

### Prerequisites
- GitHub account
- Render account (sign up at https://render.com)
- Your project pushed to GitHub

---

## Step 1: Push Your Code to GitHub

If you haven't already, push your code to GitHub:

```bash
cd /Users/aunghtet/Desktop/projects/test2

# Initialize git if not already done
git init

# Add all files
git add .

# Commit
git commit -m "Prepare for Render deployment"

# Add remote (replace with your GitHub repository URL)
git remote add origin https://github.com/YOUR_USERNAME/ktuvoting-app.git

# Push to GitHub
git push -u origin master
```

---

## Step 2: Deploy to Render

### Option A: Using render.yaml (Recommended - Automatic Setup)

1. **Go to Render Dashboard**
   - Visit https://dashboard.render.com

2. **Create New Blueprint**
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Render will automatically detect `render.yaml`

3. **Configure**
   - Render will create:
     - PostgreSQL database (`ktuvoting-db`)
     - Web service (`ktuvoting-app`)
   - Click "Apply" to deploy

4. **Wait for Deployment**
   - Database will be created first (~2-3 minutes)
   - Then the application will build and deploy (~5-10 minutes)

5. **Initialize Database**
   - Once deployed, you need to run the database initialization script
   - Go to your database in Render dashboard
   - Click "Connect" → "External Connection"
   - Copy the connection string and run:
   
   ```bash
   # Connect to Render PostgreSQL
   psql "YOUR_RENDER_DATABASE_CONNECTION_STRING" -f db.sql
   ```
   
   Or use Render's web shell:
   - Go to database → "Shell"
   - Copy and paste contents of `db.sql`

### Option B: Manual Setup

If you prefer manual setup:

#### 2.1 Create PostgreSQL Database

1. In Render Dashboard, click "New +" → "PostgreSQL"
2. Configure:
   - **Name**: `ktuvoting-db`
   - **Database**: `ktuvoting`
   - **User**: `ktuvote_user`
   - **Region**: Choose closest to your users
   - **Plan**: Free
3. Click "Create Database"
4. Wait for database to be ready

#### 2.2 Initialize Database Schema

1. Click on your database → "Connect" → "External Connection"
2. Copy the connection string
3. Run the initialization script:

```bash
psql "YOUR_CONNECTION_STRING" -f db.sql
```

Or use Render's Shell tab in the database dashboard.

#### 2.3 Create Web Service

1. Click "New +" → "Web Service"
2. Connect your GitHub repository
3. Configure:
   - **Name**: `ktuvoting-app`
   - **Environment**: `Docker`
   - **Region**: Same as database
   - **Branch**: `master` (or your main branch)
   - **Dockerfile Path**: `Dockerfile`
   - **Plan**: Free

#### 2.4 Add Environment Variables

In the Environment section, add:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `PORT` | `8080` |
| `SPRING_DATASOURCE_URL` | From database (Internal Connection String) |
| `SPRING_DATASOURCE_USERNAME` | From database |
| `SPRING_DATASOURCE_PASSWORD` | From database |
| `ADMIN_PIN` | Your secure admin PIN (e.g., `99999`) |
| `JAVA_OPTS` | `-XX:+UseG1GC -Xms512m -Xmx1g` |

**To get database connection details:**
- Go to your `ktuvoting-db` database
- Click "Connect" → "Internal Connection"
- Copy the values

#### 2.5 Deploy

Click "Create Web Service" and wait for deployment.

---

## Step 3: Verify Deployment

1. **Check Build Logs**
   - Watch the build process in Render dashboard
   - Look for "Build succeeded" message

2. **Check Application Logs**
   - Once deployed, check the logs for:
   ```
   Started KtuVotingappApplication in X seconds
   ```

3. **Test Your Application**
   - Visit your Render URL: `https://ktuvoting-app.onrender.com`
   - Try accessing the PIN entry page
   - Test voting workflow

4. **Verify Database**
   - Use Render's database shell to check:
   ```sql
   SELECT category, COUNT(*) FROM candidates GROUP BY category;
   ```
   - Should show 9 candidates for each category (45 total)

---

## Step 4: Configure Custom Domain (Optional)

1. Go to your web service settings
2. Click "Custom Domain"
3. Add your domain and follow DNS instructions

---

## Important Notes

### Free Tier Limitations

- **Database**: 1GB storage, expires after 90 days (backup your data!)
- **Web Service**: Spins down after 15 minutes of inactivity
- **Cold starts**: First request after inactivity takes 30-60 seconds

### For Production Use

Consider upgrading to paid plans for:
- Always-on services (no cold starts)
- More RAM and CPU
- Persistent database without expiration
- Better performance for high traffic

### Recommended Upgrades for 1500+ Users

- **Database**: Starter ($7/month) - 256MB RAM, no expiration
- **Web Service**: Starter ($7/month) - 512MB RAM, always on
- Or higher plans for better performance

---

## Troubleshooting

### Build Fails

**Problem**: Maven build fails
**Solution**: Check `pom.xml` dependencies and Java version (21)

### Database Connection Failed

**Problem**: App can't connect to database
**Solution**: 
- Verify environment variables are set correctly
- Use "Internal Connection String" not "External"
- Check database is in the same region

### Images Not Loading

**Problem**: Candidate photos not displaying
**Solution**: 
- Ensure images are in `src/main/resources/static/images/`
- Check image files are committed to Git
- Verify image URLs in database match filenames

### Application Slow/Timeout

**Problem**: First request very slow
**Solution**: 
- This is normal for free tier (cold start)
- Consider upgrading to always-on service
- Implement health check pings to keep service warm

### Database Not Initialized

**Problem**: No candidates showing
**Solution**:
- Connect to database shell
- Run the `db.sql` script manually
- Check database logs for errors

---

## Monitoring

### Health Check

Your app exposes health endpoint at:
```
https://your-app.onrender.com/actuator/health
```

### Admin Results

Check voting results at:
```
https://your-app.onrender.com/admin?pin=YOUR_ADMIN_PIN
```

### Database Monitoring

- Go to database dashboard in Render
- Check "Metrics" tab for connection count, queries, etc.

---

## Backup & Maintenance

### Backup Database

```bash
# Export database
pg_dump "YOUR_RENDER_DATABASE_CONNECTION_STRING" > backup.sql

# Restore if needed
psql "YOUR_RENDER_DATABASE_CONNECTION_STRING" < backup.sql
```

### Clear Old Votes

```bash
# Run CLEAR_DATABASE.sql to reset
psql "YOUR_RENDER_DATABASE_CONNECTION_STRING" -f CLEAR_DATABASE.sql
psql "YOUR_RENDER_DATABASE_CONNECTION_STRING" -f db.sql
```

---

## Security Checklist

- ✅ Change default ADMIN_PIN
- ✅ Use strong database password
- ✅ Enable HTTPS (automatic on Render)
- ✅ Review environment variables (no secrets in code)
- ✅ Limit database access to internal connections only

---

## Support

- **Render Docs**: https://render.com/docs
- **Render Community**: https://community.render.com
- **GitHub Issues**: Create issue in your repository

---

## Next Steps After Deployment

1. **Test thoroughly** with different browsers and devices
2. **Share the URL** with a small test group first
3. **Monitor logs** during test voting
4. **Set up custom domain** if needed
5. **Plan for scaling** if expecting high traffic

---

## Example: Complete Deployment Command Sequence

```bash
# 1. Ensure code is ready
cd /Users/aunghtet/Desktop/projects/test2

# 2. Commit any changes
git add .
git commit -m "Ready for Render deployment"
git push origin master

# 3. Deploy on Render (via web dashboard)
# Follow Option A or B above

# 4. After deployment, initialize database
# Get connection string from Render dashboard, then:
psql "postgresql://user:pass@host/database" -f db.sql

# 5. Test
curl https://your-app.onrender.com/actuator/health

# Done! 🎉
```

---

Good luck with your deployment! 🚀

