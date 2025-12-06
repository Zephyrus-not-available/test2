# Database Design - Voting System

## Overview
This document describes the optimized database design for the voting system with device ID tracking and bulk voting capabilities.

## Database Schema

### 1. Voters Table (`voters`)
Stores voter information with device tracking to prevent duplicate voting.

**Columns:**
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - Unique voter identifier
- `pin` (VARCHAR(5), NOT NULL, UNIQUE) - Voter PIN code
- `device_id` (VARCHAR(255), NOT NULL, UNIQUE) - Unique device identifier (prevents duplicate voting)
- `has_voted` (BOOLEAN, NOT NULL, DEFAULT false) - Voting status flag
- `created_at` (TIMESTAMP) - Voter registration timestamp
- `voted_at` (TIMESTAMP) - First vote submission timestamp

**Indexes:**
- `idx_pin` - Index on `pin` for fast PIN lookups
- `idx_has_voted` - Index on `has_voted` for voter statistics queries
- `idx_device_id` - Index on `device_id` for device validation queries
- `uk_device_id` - UNIQUE constraint on `device_id` (prevents duplicate device voting)

**Key Design Decisions:**
- **Unique constraint on `device_id`**: Ensures one device can only vote once, preventing fraud
- **Separate `pin` and `device_id`**: Allows flexible voter identification while maintaining device security
- **Indexes**: Optimized for common query patterns (PIN lookup, device validation, statistics)

### 2. Votes Table (`votes`)
Stores individual vote records linked to voters and candidates.

**Columns:**
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - Unique vote identifier
- `voter_id` (BIGINT, NOT NULL, FOREIGN KEY) - Reference to voter
- `category` (VARCHAR(20), NOT NULL) - Vote category (KING, QUEEN, PRINCE, PRINCESS, COUPLE)
- `candidate_id` (BIGINT, NOT NULL, FOREIGN KEY) - Reference to candidate
- `created_at` (TIMESTAMP) - Vote submission timestamp

**Indexes:**
- `idx_voter_id` - Index on `voter_id` for voter vote history queries
- `idx_category` - Index on `category` for category-based queries
- `idx_candidate_id` - Index on `candidate_id` for candidate vote counts
- `idx_voter_category` - Composite index on `(voter_id, category)` for fast duplicate checking

**Unique Constraints:**
- `uk_voter_category` - UNIQUE constraint on `(voter_id, category)` - **Database-level enforcement** prevents duplicate votes even under concurrent load

**Key Design Decisions:**
- **Foreign keys**: Maintain referential integrity between votes, voters, and candidates
- **Separate vote records**: Allows one voter to vote in multiple categories
- **Indexes**: Optimized for result aggregation and validation queries

### 3. Candidates Table (`candidates`)
Stores candidate information and vote counts.

**Columns:**
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - Unique candidate identifier
- `category` (VARCHAR(20), NOT NULL) - Candidate category
- `candidate_number` (INTEGER, NOT NULL) - Candidate number within category
- `name` (VARCHAR(100), NOT NULL) - Candidate name
- `department` (VARCHAR(100)) - Department affiliation
- `image_url` (VARCHAR(500)) - Candidate image URL
- `vote_count` (BIGINT, DEFAULT 0) - Cached vote count

**Indexes:**
- `idx_category_number` - Composite index on `(category, candidate_number)` for unique candidate lookup

**Key Design Decisions:**
- **Cached vote count**: Denormalized for performance (updated via application logic)
- **Composite index**: Ensures fast candidate lookup by category and number

## Database Constraints & Integrity

### Unique Constraints
1. **`voters.device_id` (UNIQUE)**: Primary mechanism to prevent duplicate device voting - **Database-enforced**
2. **`voters.pin` (UNIQUE)**: Ensures unique PIN codes - **Database-enforced**
3. **`votes(voter_id, category)` (UNIQUE)**: Prevents duplicate votes per category per voter - **Database-enforced** - Critical for concurrent voting

**Important:** All uniqueness constraints are enforced at the database level to prevent race conditions when 1500+ users vote simultaneously.

### Foreign Key Constraints
- `votes.voter_id` → `voters.id` (CASCADE on delete optional)
- `votes.candidate_id` → `candidates.id` (CASCADE on delete optional)

### Transaction Safety
- All voting operations use `@Transactional` to ensure atomicity
- Bulk voting commits all votes in a single transaction (all or nothing)

## Anti-Fraud Mechanisms & Concurrency Protection

### 1. Device ID Tracking
- Each vote submission requires a unique `device_id`
- **Database-level UNIQUE constraint** prevents duplicate device registration
- **Pessimistic locking** prevents race conditions during device checks
- One device can only vote once across all categories

### 2. Atomic Bulk Voting
- All votes in a bulk request are processed in a **single transaction**
- **READ_COMMITTED isolation level** ensures data consistency
- If any validation fails, entire transaction rolls back
- Ensures data consistency and prevents partial votes

### 3. Category-Level Validation
- **Database-level UNIQUE constraint** on `(voter_id, category)` enforces one vote per category
- **Pessimistic locking** prevents concurrent duplicate vote attempts
- Prevents multiple votes in the same category even under extreme concurrent load

### 4. Pessimistic Locking
- Critical sections use `SELECT ... FOR UPDATE` (PESSIMISTIC_WRITE)
- Prevents race conditions during:
    - Device ID validation
    - Voter creation/update
    - Vote existence checks
- Ensures sequential processing of concurrent requests for same resources

### 5. Exception Handling
- **DataIntegrityViolationException** caught and handled gracefully
- Database constraint violations return user-friendly error messages
- State verification after constraint violations ensures accuracy

## Performance Optimizations

### Indexing Strategy
- **Query Optimization**: All foreign keys and commonly filtered columns are indexed
- **Composite Indexes**: Used where multiple columns are queried together
- **Statistics Queries**: `has_voted` index enables fast voter statistics

### Caching Strategy
- Vote counts cached in `candidates.vote_count` column
- Cache invalidation on vote submission
- Reduces aggregation queries during result retrieval

## API Usage Examples

### Single Vote Submission
```json
POST /api/voting/vote
{
  "deviceId": "unique-device-id-12345",
  "pin": "12345",
  "category": "KING",
  "candidateNumber": 1
}
```

### Bulk Vote Submission (All at Once)
```json
POST /api/voting/bulk-vote
{
  "deviceId": "unique-device-id-12345",
  "pin": "12345",
  "votes": [
    {
      "category": "KING",
      "candidateNumber": 1
    },
    {
      "category": "QUEEN",
      "candidateNumber": 2
    },
    {
      "category": "PRINCE",
      "candidateNumber": 3
    }
  ]
}
```

### Check Device Voting Status
```
GET /api/voting/device-has-voted?deviceId=unique-device-id-12345
```

## Migration Notes

### Breaking Changes
- `VoteRequest` now requires `deviceId` field
- Existing voters without `device_id` will need migration
- Database migration needed to add `device_id` column and constraints

### Migration Script (Example SQL)
```sql
-- Add device_id column
ALTER TABLE voters 
ADD COLUMN device_id VARCHAR(255);

-- Create index
CREATE INDEX idx_device_id ON voters(device_id);

-- Add unique constraint (after handling existing data)
-- Note: This requires generating device_ids for existing voters or handling them separately
ALTER TABLE voters 
ADD CONSTRAINT uk_device_id UNIQUE (device_id);
```

## Best Practices

1. **Device ID Generation**: Use UUIDs or secure random strings for device IDs
2. **Transaction Management**: Always use `@Transactional` for vote operations
3. **Error Handling**: Handle unique constraint violations gracefully
4. **Validation**: Validate all votes before processing in bulk operations
5. **Monitoring**: Track device ID conflicts for potential fraud detection

