# Architecture Overview - King & Queen Voting System

## System Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Connection Pool**: HikariCP (configured for 1500+ users)
- **Caching**: Caffeine Cache
- **API**: RESTful APIs with JSON

### Frontend
- **HTML/CSS/JavaScript**: Static files
- **Styling**: Tailwind CSS
- **API Integration**: Fetch API for REST calls

## Database Schema

### Tables

1. **voters**
    - `id` (Primary Key)
    - `pin` (Unique, 5 digits)
    - `has_voted` (Boolean)
    - `created_at` (Timestamp)
    - `voted_at` (Timestamp)
    - **Indexes**: `pin`, `has_voted`

2. **candidates**
    - `id` (Primary Key)
    - `category` (Enum: KING, QUEEN, PRINCE, PRINCESS, COUPLE)
    - `candidate_number` (1-9)
    - `name` (String)
    - `department` (String)
    - `image_url` (String)
    - `vote_count` (Long)
    - **Indexes**: `category`, `candidate_number`

3. **votes**
    - `id` (Primary Key)
    - `voter_id` (Foreign Key → voters)
    - `category` (Enum)
    - `candidate_id` (Foreign Key → candidates)
    - `created_at` (Timestamp)
    - **Indexes**: `voter_id`, `category`, `candidate_id`

## API Endpoints

### Authentication
- `POST /api/auth/verify-pin`
    - Request: `{ "pin": "12345" }`
    - Response: `{ "valid": true, "role": "VOTER", "hasVoted": false }`

### Candidates
- `GET /api/candidates/{category}`
    - Categories: KING, QUEEN, PRINCE, PRINCESS, COUPLE
    - Response: Array of candidate objects

### Voting
- `POST /api/voting/vote`
    - Request: `{ "pin": "12345", "category": "KING", "candidateNumber": 1 }`
    - Response: `{ "success": true, "message": "Vote submitted successfully" }`

- `GET /api/voting/has-voted?pin={pin}&category={category}`
    - Response: `true` or `false`

### Results
- `GET /api/results/{category}`
    - Response: Results with vote counts and percentages

- `GET /api/results/all`
    - Response: All results for all categories

## Performance Optimizations

### Database
- **Indexes** on frequently queried columns
- **Batch processing** enabled for bulk operations
- **Connection pooling** with HikariCP (50 max connections)

### Caching
- **Caffeine Cache** for:
    - Candidates by category (5-minute TTL)
    - Voting results (5-minute TTL)
    - Voter lookups (5-minute TTL)

### Application
- **Async processing** enabled
- **Transaction management** for data consistency
- **Connection pool monitoring** enabled

## Scalability Features

1. **Connection Pooling**: 50 max connections, handles 1500+ concurrent users
2. **Caching**: Reduces database load for frequently accessed data
3. **Indexed Queries**: Fast lookups on critical columns
4. **Batch Operations**: Efficient bulk inserts/updates
5. **Stateless API**: Easy horizontal scaling

## Security Considerations

1. **PIN-based Authentication**: Simple but effective for voting
2. **Duplicate Vote Prevention**: One vote per category per PIN
3. **CORS Configuration**: Configurable for production
4. **Input Validation**: Request validation on all endpoints

## Deployment Architecture

```
┌─────────────┐
│   Browser   │
│  (Frontend) │
└──────┬──────┘
       │ HTTP/REST
       │
┌──────▼──────────┐
│  Spring Boot    │
│   Application   │
│   (Port 8080)   │
└──────┬──────────┘
       │ JDBC
       │
┌──────▼──────────┐
│   PostgreSQL    │
│   Database      │
│  (Port 5432)    │
└─────────────────┘
```

## File Structure

```
voting_project/
├── src/main/java/com/ktu/voting/
│   ├── config/          # Configuration classes
│   ├── controller/       # REST controllers
│   ├── dto/              # Data Transfer Objects
│   ├── model/            # JPA entities
│   ├── repository/       # Data repositories
│   ├── service/          # Business logic
│   └── VotingSystemApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── data.sql          # Initial data
├── api-config.js         # Frontend API configuration
├── selection-helper.js    # Frontend helper functions
├── *.html                 # Frontend pages
└── pom.xml               # Maven configuration
```

## Future Enhancements

1. **Authentication**: JWT tokens for secure API access
2. **Rate Limiting**: Prevent abuse and DDoS attacks
3. **Real-time Updates**: WebSocket for live result updates
4. **Analytics**: Voting patterns and statistics
5. **Multi-language Support**: Internationalization
6. **Mobile App**: Native mobile application
7. **Load Balancing**: Multiple backend instances
8. **Database Replication**: High availability

