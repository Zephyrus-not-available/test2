# Quick Testing Guide

## Test PIN Number

**PIN: `12345`**

This is the default test PIN. You can use it on multiple devices - each device will get its own unique voter record.

## Setup Steps

### 1. Check if PIN exists in database

Connect to your PostgreSQL database and run:

```sql
SELECT pin, device_id, has_voted FROM voters WHERE pin = '12345';
```

### 2. Create PIN if it doesn't exist

Run this SQL:

```sql
INSERT INTO voters (pin, device_id, has_voted)
SELECT '12345', 'seed-' || EXTRACT(EPOCH FROM NOW())::BIGINT, FALSE
WHERE NOT EXISTS (SELECT 1 FROM voters WHERE pin = '12345' LIMIT 1);
```

### 3. Test the Voting Flow

1. Start your application: `mvn spring-boot:run`
2. Open browser: `http://localhost:8080`
3. Enter PIN: `12345`
4. Make selections on all pages (King, Queen, Prince, Princess, Couple)
5. Click "Confirm" button
6. Check success page

### 4. Verify Votes Were Saved

Run these queries in your database:

```sql
-- Check voters
SELECT id, pin, device_id, has_voted, created_at, voted_at 
FROM voters 
WHERE pin = '12345'
ORDER BY created_at DESC;

-- Check votes
SELECT v.pin, v.device_id, vo.category, c.candidate_number, c.name, vo.created_at
FROM votes vo
JOIN voters v ON vo.voter_id = v.id
JOIN candidates c ON vo.candidate_id = c.id
WHERE v.pin = '12345'
ORDER BY vo.created_at DESC;
```

## Testing Shared PIN Feature

1. **Open in multiple browsers/devices:**
   - Browser 1: Enter PIN `12345` → Vote
   - Browser 2: Enter PIN `12345` → Vote (should work independently)
   - Same browser again: Enter PIN `12345` → Should say already voted

2. **Check database:**
   ```sql
   -- You should see multiple voters with same PIN, different device IDs
   SELECT pin, device_id, has_voted 
   FROM voters 
   WHERE pin = '12345';
   ```

## Using a Different PIN

If you want to use a different PIN (e.g., `99999`), run:

```sql
INSERT INTO voters (pin, device_id, has_voted)
SELECT '99999', 'seed-' || EXTRACT(EPOCH FROM NOW())::BIGINT, FALSE
WHERE NOT EXISTS (SELECT 1 FROM voters WHERE pin = '99999' LIMIT 1);
```

Then use `99999` as your test PIN.

## Troubleshooting

**Issue: PIN not accepted**
- Check if PIN exists: `SELECT * FROM voters WHERE pin = '12345';`
- If empty, run the INSERT SQL above

**Issue: Votes not saving**
- Check browser console (F12) for JavaScript errors
- Check application logs for backend errors
- Verify database connection in `application.properties`

**Issue: Device already voted**
- This is expected! Each device can only vote once
- Use a different browser/device to test again
- Or clear the device ID: Open browser console, run `localStorage.removeItem('votingDeviceId')`

## Quick Commands

```bash
# Start application
mvn spring-boot:run

# Connect to database (adjust connection details)
psql -U postgres -d ktuvoting

# Check all voters
SELECT * FROM voters ORDER BY created_at DESC;

# Check all votes
SELECT v.pin, vo.category, c.name, vo.created_at 
FROM votes vo
JOIN voters v ON vo.voter_id = v.id
JOIN candidates c ON vo.candidate_id = c.id
ORDER BY vo.created_at DESC;
```

