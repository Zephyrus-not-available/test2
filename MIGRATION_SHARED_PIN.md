# Migration Guide: Shared PIN Support

This guide explains the changes made to support shared PINs (one PIN for all users) and device ID generation only on Confirm button click.

## Changes Made

### 1. Device ID Generation
- **Before**: Device ID was generated when user first visited any selection page
- **After**: Device ID is generated only when user clicks the "Confirm" button on the summary page
- **Impact**: Each device gets a unique ID only at vote submission time

### 2. Shared PIN Support
- **Before**: Each PIN was unique (one PIN per voter)
- **After**: Multiple devices can use the same PIN (shared PIN for all users)
- **Impact**: Admin can provide one PIN that all users can use

### 3. Database Schema Changes
- **Removed**: Unique constraint on `pin` column in `voters` table
- **Kept**: Unique constraint on `device_id` column (prevents duplicate votes from same device)

## Database Migration

If you have an existing database, run this SQL script to update the schema:

```sql
-- Remove unique constraint on PIN (allow shared PINs)
ALTER TABLE voters DROP CONSTRAINT IF EXISTS uk_pin;
ALTER TABLE voters DROP CONSTRAINT IF EXISTS voters_pin_key;

-- Verify device_id still has unique constraint
-- (This should already exist, but verify with:)
-- \d voters
```

## How It Works Now

### Voting Flow:
1. User enters shared PIN (e.g., "12345")
2. User makes selections on all pages
3. User clicks "Confirm" button
4. **Device ID is generated at this point** (unique per device)
5. Votes are submitted with: PIN (shared) + Device ID (unique)
6. Backend creates/updates voter record:
   - Finds voter by device ID (not PIN)
   - If device doesn't exist, creates new voter with PIN and device ID
   - If device exists, uses existing voter
7. Votes are saved to `votes` table linked to the voter

### Example:
- User A on Device 1: PIN="12345", DeviceID="uuid-1" → Creates Voter 1
- User B on Device 2: PIN="12345", DeviceID="uuid-2" → Creates Voter 2
- User C on Device 1 again: PIN="12345", DeviceID="uuid-1" → Uses Voter 1 (already voted)

## Testing

### Test Shared PIN:
1. Use the same PIN on multiple devices/browsers
2. Each device should be able to vote independently
3. Same device cannot vote twice (device ID prevents this)

### Test Device ID Generation:
1. Open browser and go through voting flow
2. Check browser localStorage - `votingDeviceId` should NOT exist until Confirm is clicked
3. After clicking Confirm, check that `votingDeviceId` exists
4. Check database - new voter record should be created with device ID

## Verification Queries

```sql
-- Check all voters (should see multiple with same PIN)
SELECT id, pin, device_id, has_voted, created_at, voted_at 
FROM voters 
ORDER BY created_at DESC;

-- Check votes by PIN (should see votes from multiple devices)
SELECT v.pin, v.device_id, vo.category, c.candidate_number, c.name
FROM votes vo
JOIN voters v ON vo.voter_id = v.id
JOIN candidates c ON vo.candidate_id = c.id
WHERE v.pin = '12345'
ORDER BY vo.created_at DESC;

-- Count voters per PIN (should be > 1 for shared PIN)
SELECT pin, COUNT(*) as voter_count
FROM voters
GROUP BY pin
ORDER BY voter_count DESC;
```

## Rollback (if needed)

If you need to revert to unique PINs:

```sql
-- Add back unique constraint on PIN
ALTER TABLE voters ADD CONSTRAINT uk_pin UNIQUE (pin);

-- Note: This will fail if you have duplicate PINs
-- You'll need to clean up duplicate PINs first
```

## Important Notes

1. **Device ID is unique**: Each device can only vote once (enforced by unique constraint on `device_id`)
2. **PIN can be shared**: Multiple devices can use the same PIN
3. **Device ID generated on Confirm**: Not generated until user clicks Confirm button
4. **Backward compatible**: Existing voters with unique PINs will continue to work
5. **No data loss**: Existing votes are preserved

## Troubleshooting

### Issue: "Device has already been used to vote"
- **Cause**: Same device trying to vote again
- **Solution**: This is expected behavior - each device can only vote once

### Issue: Multiple voters with same PIN not created
- **Cause**: Database migration not run
- **Solution**: Run the migration SQL to remove PIN unique constraint

### Issue: Device ID not generated
- **Cause**: JavaScript error or localStorage disabled
- **Solution**: Check browser console for errors, ensure localStorage is enabled

