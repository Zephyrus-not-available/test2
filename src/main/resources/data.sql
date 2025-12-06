-- Sample Candidates Data
-- This file will be executed on application startup after schema creation
-- Note: For production, use Flyway or Liquibase for proper database migrations

-- KING Candidates
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'KING', 1, 'Candidate King 1', 'Computer Science', '/images/king1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'KING', 2, 'Candidate King 2', 'Electronics', '/images/king2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'KING', 3, 'Candidate King 3', 'Mechanical', '/images/king3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 3);

-- QUEEN Candidates
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'QUEEN', 1, 'Candidate Queen 1', 'Computer Science', '/images/queen1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'QUEEN', 2, 'Candidate Queen 2', 'Electronics', '/images/queen2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'QUEEN', 3, 'Candidate Queen 3', 'Mechanical', '/images/queen3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 3);

-- PRINCE Candidates
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCE', 1, 'Candidate Prince 1', 'Computer Science', '/images/prince1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCE', 2, 'Candidate Prince 2', 'Electronics', '/images/prince2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCE', 3, 'Candidate Prince 3', 'Mechanical', '/images/prince3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 3);

-- PRINCESS Candidates
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCESS', 1, 'Candidate Princess 1', 'Computer Science', '/images/princess1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCESS', 2, 'Candidate Princess 2', 'Electronics', '/images/princess2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCESS', 3, 'Candidate Princess 3', 'Mechanical', '/images/princess3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 3);

-- COUPLE Candidates
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'COUPLE', 1, 'Couple 1', 'Computer Science', '/images/couple1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'COUPLE', 2, 'Couple 2', 'Electronics', '/images/couple2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'COUPLE', 3, 'Couple 3', 'Mechanical', '/images/couple3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 3);

-- Sample Voters (Optional - for testing)
-- Uncomment and modify as needed
-- INSERT INTO voters (pin, device_id, has_voted, created_at) 
-- SELECT '12345', 'device-12345', false, CURRENT_TIMESTAMP
-- WHERE NOT EXISTS (SELECT 1 FROM voters WHERE pin = '12345');
