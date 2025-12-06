# Concurrency & Performance Guide

## Overview
This document describes the concurrency protections and performance optimizations implemented to handle 1500+ concurrent users voting simultaneously without data loss or duplication.

## Problem Statement
When 1500 users vote at the same time, we need to prevent:
1. **Duplicate votes** - Same device/voter voting multiple times
2. **Missing votes** - Votes lost due to race conditions
3. **Data inconsistency** - Vote counts not matching actual votes
4. **Performance degradation** - System slowdown under high load

## Solutions Implemented

### 1. Database-Level Constraints (Primary Defense)

#### Unique Constraints
```sql
-- Prevents duplicate device voting
ALTER TABLE voters ADD CONSTRAINT uk_device_id UNIQUE (device_id);

-- Prevents duplicate votes per category per voter
ALTER TABLE votes ADD CONSTRAINT uk_voter_category UNIQUE (voter_id, category);
```

**Benefits:**
- **Guaranteed uniqueness** at database level
- **Atomic enforcement** - no application-level race conditions
- **Database-level validation** - catches duplicates even if application logic fails

### 2. Pessimistic Locking (Race Condition Prevention)

#### Implementation
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT v FROM Voter v WHERE v.deviceId = :deviceId")
Optional<Voter> findByDeviceIdWithLock(@Param("deviceId") String deviceId);
```

**How it works:**
- Uses `SELECT ... FOR UPDATE` (PostgreSQL) or equivalent
- **Locks the row** during transaction
- **Prevents concurrent modifications** to the same voter
- Other transactions **wait** until lock is released

**When to use:**
- Device ID validation (critical section)
- Vote existence checks
- Voter creation/update operations

### 3. Transaction Isolation Levels

#### READ_COMMITTED Isolation
```java
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
```

**Benefits:**
- **Balanced** between consistency and performance
- **Prevents dirty reads** - only committed data visible
- **Better performance** than SERIALIZABLE
- **Works with pessimistic locking** for race condition prevention

### 4. Connection Pool Optimization

#### Configuration (application.properties)
```properties
# Optimized for 1500 concurrent users
spring.datasource.hikari.maximum-pool-size=100
spring.datasource.hikari.minimum-idle=20
spring.datasource.hikari.connection-timeout=20000
```

**Strategy:**
- **100 max connections** - handles peak concurrent transactions
- **20 minimum idle** - ready connections for immediate use
- **20s timeout** - reasonable wait time during peak load
- **Connection validation** - ensures connections are healthy

**Calculation:**
- Average transaction time: ~200ms
- Peak concurrent requests: 1500
- Required connections: ~30-50 (with proper queuing)
- Buffer: 100 connections provides 2-3x headroom

### 5. Exception Handling & Retry Logic

#### DataIntegrityViolationException Handling
```java
try {
    voteRepository.save(vote);
} catch (DataIntegrityViolationException e) {
    // Handle duplicate detected at database level
    // Verify actual state and return appropriate response
}
```

**Benefits:**
- **Graceful degradation** - returns meaningful error messages
- **No silent failures** - all exceptions handled
- **State verification** - double-checks after constraint violations
- **User-friendly responses** - clear error messages

### 6. Batch Processing Optimization

#### Configuration
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

**Benefits:**
- **Reduced database round-trips** - batches multiple operations
- **Improved throughput** - processes more votes per transaction
- **Optimized SQL** - fewer prepared statements

### 7. Caching Strategy

#### Cache Invalidation on Vote
```java
@CacheEvict(value = {"results", "candidates"}, allEntries = true)
```

**Benefits:**
- **Immediate consistency** - cache cleared after vote
- **Prevents stale data** - results always accurate
- **Reduces database load** - cached data for reads

## Concurrency Flow Diagram

```
User Request → Controller
    ↓
Pessimistic Lock (Device Check)
    ↓
    ┌─────────────────┐
    │ Device Exists?  │
    └────────┬────────┘
             │
    ┌────────▼────────┐
    │   YES → REJECT  │
    └─────────────────┘
             │
    ┌────────▼────────┐
    │   NO → Continue │
    └────────┬────────┘
             │
Pessimistic Lock (Voter Check)
    ↓
Create/Update Voter
    ↓
Pessimistic Lock (Vote Check)
    ↓
Save Vote (DB Constraint Check)
    ↓
    ┌─────────────────────┐
    │ Constraint Violated?│
    └────────┬────────────┘
             │
    ┌────────▼────────┐
    │  YES → REJECT   │
    └─────────────────┘
             │
    ┌────────▼────────┐
    │  NO → SUCCESS   │
    └─────────────────┘
```

## Performance Metrics

### Expected Performance (1500 concurrent users)

| Metric | Target | Notes |
|--------|--------|-------|
| Response Time (p95) | < 500ms | Including database operations |
| Throughput | > 3000 req/min | Sustained voting rate |
| Connection Pool Usage | < 80% | Peak usage during rush |
| Error Rate | < 0.1% | Constraint violations handled |
| Duplicate Rate | 0% | Enforced at DB level |

### Monitoring Points

1. **Connection Pool Metrics**
    - Active connections
    - Idle connections
    - Wait time for connections

2. **Transaction Metrics**
    - Transaction duration
    - Rollback rate
    - Lock wait time

3. **Database Metrics**
    - Query execution time
    - Constraint violation count
    - Lock contention

## Testing Concurrency

### Load Testing Script
```bash
# Using Apache Bench (ab) for 1500 concurrent requests
ab -n 1500 -c 50 -p vote_request.json -T application/json \
   http://localhost:8080/api/voting/bulk-vote
```

### What to Verify
1. ✅ No duplicate votes in database
2. ✅ All valid votes are saved
3. ✅ Vote counts match actual votes
4. ✅ No missing votes
5. ✅ Response times within acceptable range
6. ✅ No database deadlocks

## Best Practices

### Do's ✅
- Always use pessimistic locking for critical sections
- Handle DataIntegrityViolationException gracefully
- Monitor connection pool usage
- Use transactions for atomic operations
- Verify state after constraint violations

### Don'ts ❌
- Don't skip database-level constraints
- Don't ignore exception handling
- Don't use optimistic locking for vote operations
- Don't create voters without checking device ID first
- Don't process votes outside transactions

## Troubleshooting

### Issue: High Lock Wait Times
**Symptom:** Requests timing out, slow responses
**Solution:**
- Increase connection pool size
- Optimize transaction duration
- Review lock acquisition order

### Issue: Constraint Violation Errors
**Symptom:** DataIntegrityViolationException in logs
**Solution:**
- Verify device ID uniqueness
- Check for duplicate submissions
- Ensure proper exception handling

### Issue: Missing Votes
**Symptom:** Vote count doesn't match submissions
**Solution:**
- Check transaction rollback logs
- Verify exception handling
- Review database constraint enforcement

## Database Schema Enhancements

### Indexes for Performance
```sql
-- Fast device lookup
CREATE INDEX idx_device_id ON voters(device_id);

-- Fast vote lookup per voter/category
CREATE INDEX idx_voter_category ON votes(voter_id, category);

-- Fast candidate lookup
CREATE INDEX idx_category_number ON candidates(category, candidate_number);
```

### Constraints for Data Integrity
```sql
-- Unique device constraint
ALTER TABLE voters ADD CONSTRAINT uk_device_id UNIQUE (device_id);

-- Unique vote per category per voter
ALTER TABLE votes ADD CONSTRAINT uk_voter_category UNIQUE (voter_id, category);
```

## Conclusion

The system is designed to handle 1500+ concurrent users through:
1. **Database-level constraints** (ultimate protection)
2. **Pessimistic locking** (race condition prevention)
3. **Optimized connection pooling** (performance)
4. **Proper exception handling** (graceful degradation)
5. **Transaction management** (atomicity)

All these layers work together to ensure **zero data loss** and **zero duplicates** even under extreme concurrent load.

