# Database Update Guide - Refresh Candidate Data

## What Changed?
✅ Updated `data.sql` to include **9 candidates** for each category instead of just 3:
- KING: 1-9 candidates
- QUEEN: 1-9 candidates  
- PRINCE: 1-9 candidates
- PRINCESS: 1-9 candidates
- COUPLE: 1-9 candidates

✅ Added `WebConfig.java` to properly serve static resources (images, js, css)

## Steps to Apply Changes:

### Option 1: Quick Reset (Recommended for Development)

1. **Stop your application** if it's running

2. **Connect to PostgreSQL** and clear old data:
   ```bash
   psql -U postgres -d ktuvoting
   ```

3. **Run these SQL commands:**
   ```sql
   -- Clear all votes and candidates
   DELETE FROM votes;
   DELETE FROM candidates;
   
   -- Clear voters if needed (optional)
   DELETE FROM voters;
   ```

4. **Exit PostgreSQL:**
   ```sql
   \q
   ```

5. **Start your application** - it will automatically load all 45 candidates (9 x 5 categories):
   ```bash
   cd /Users/aunghtet/Desktop/projects/test2
   ./mvnw spring-boot:run
   ```

### Option 2: Drop and Recreate Database (Clean Slate)

1. **Stop your application**

2. **Connect to PostgreSQL:**
   ```bash
   psql -U postgres
   ```

3. **Drop and recreate the database:**
   ```sql
   DROP DATABASE IF EXISTS ktuvoting;
   CREATE DATABASE ktuvoting;
   \q
   ```

4. **Start your application** - it will create all tables and load the 45 candidates:
   ```bash
   cd /Users/aunghtet/Desktop/projects/test2
   ./mvnw spring-boot:run
   ```

## Verification

After starting the application, verify the data:

```sql
-- Connect to database
psql -U postgres -d ktuvoting

-- Check candidate counts
SELECT category, COUNT(*) as total 
FROM candidates 
GROUP BY category 
ORDER BY category;

-- You should see:
-- COUPLE   | 9
-- KING     | 9
-- PRINCE   | 9
-- PRINCESS | 9
-- QUEEN    | 9
```

## Image Files Required

Make sure you have these images in `src/main/resources/static/images/`:
- king1.jpg to king9.jpg
- queen1.jpg to queen9.jpg
- prince1.jpg to prince9.jpg
- princess1.jpg to princess9.jpg
- couple1.jpg to couple9.jpg

## Notes

- The `WHERE NOT EXISTS` clause prevents duplicate entries
- All images are served from `/images/` path
- Static resources are cached for 1 hour (3600 seconds)
- Vote counts start at 0 for all candidates

