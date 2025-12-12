-- Candidates Data - All 9 candidates for each category
-- This file will be executed on application startup after schema creation
-- Note: For production, use Flyway or Liquibase for proper database migrations

-- KING Candidates (1-9)
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'KING', 1, 'King Candidate 1', 'Computer Science', '/images/king1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'KING', 2, 'King Candidate 2', 'Electronics Engineering', '/images/king2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'KING', 3, 'King Candidate 3', 'Mechanical Engineering', '/images/king3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 3);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'KING', 4, 'King Candidate 4', 'Civil Engineering', '/images/king4.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 4);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'KING', 5, 'King Candidate 5', 'Electrical Engineering', '/images/king5.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 5);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'KING', 6, 'King Candidate 6', 'Information Technology', '/images/king6.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 6);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'KING', 7, 'King Candidate 7', 'Biotechnology', '/images/king7.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 7);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'KING', 8, 'King Candidate 8', 'Chemical Engineering', '/images/king8.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 8);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'KING', 9, 'King Candidate 9', 'Architecture', '/images/king9.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'KING' AND candidate_number = 9);

-- QUEEN Candidates (1-9)
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'QUEEN', 1, 'Queen Candidate 1', 'Computer Science', '/images/queen1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'QUEEN', 2, 'Queen Candidate 2', 'Electronics Engineering', '/images/queen2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'QUEEN', 3, 'Queen Candidate 3', 'Mechanical Engineering', '/images/queen3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 3);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'QUEEN', 4, 'Queen Candidate 4', 'Civil Engineering', '/images/queen4.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 4);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'QUEEN', 5, 'Queen Candidate 5', 'Electrical Engineering', '/images/queen5.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 5);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'QUEEN', 6, 'Queen Candidate 6', 'Information Technology', '/images/queen6.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 6);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'QUEEN', 7, 'Queen Candidate 7', 'Biotechnology', '/images/queen7.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 7);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'QUEEN', 8, 'Queen Candidate 8', 'Chemical Engineering', '/images/queen8.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 8);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'QUEEN', 9, 'Queen Candidate 9', 'Architecture', '/images/queen9.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'QUEEN' AND candidate_number = 9);

-- PRINCE Candidates (1-9)
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCE', 1, 'Prince Candidate 1', 'Computer Science', '/images/prince1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCE', 2, 'Prince Candidate 2', 'Electronics Engineering', '/images/prince2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCE', 3, 'Prince Candidate 3', 'Mechanical Engineering', '/images/prince3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 3);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCE', 4, 'Prince Candidate 4', 'Civil Engineering', '/images/prince4.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 4);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCE', 5, 'Prince Candidate 5', 'Electrical Engineering', '/images/prince5.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 5);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCE', 6, 'Prince Candidate 6', 'Information Technology', '/images/prince6.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 6);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCE', 7, 'Prince Candidate 7', 'Biotechnology', '/images/prince7.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 7);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCE', 8, 'Prince Candidate 8', 'Chemical Engineering', '/images/prince8.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 8);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCE', 9, 'Prince Candidate 9', 'Architecture', '/images/prince9.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCE' AND candidate_number = 9);

-- PRINCESS Candidates (1-9)
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCESS', 1, 'Princess Candidate 1', 'Computer Science', '/images/princess1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCESS', 2, 'Princess Candidate 2', 'Electronics Engineering', '/images/princess2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'PRINCESS', 3, 'Princess Candidate 3', 'Mechanical Engineering', '/images/princess3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 3);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCESS', 4, 'Princess Candidate 4', 'Civil Engineering', '/images/princess4.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 4);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCESS', 5, 'Princess Candidate 5', 'Electrical Engineering', '/images/princess5.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 5);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCESS', 6, 'Princess Candidate 6', 'Information Technology', '/images/princess6.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 6);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCESS', 7, 'Princess Candidate 7', 'Biotechnology', '/images/princess7.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 7);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCESS', 8, 'Princess Candidate 8', 'Chemical Engineering', '/images/princess8.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 8);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'PRINCESS', 9, 'Princess Candidate 9', 'Architecture', '/images/princess9.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'PRINCESS' AND candidate_number = 9);

-- COUPLE Candidates (1-9)
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'COUPLE', 1, 'Couple 1', 'Computer Science', '/images/couple1.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 1);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'COUPLE', 2, 'Couple 2', 'Electronics Engineering', '/images/couple2.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 2);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count) 
SELECT 'COUPLE', 3, 'Couple 3', 'Mechanical Engineering', '/images/couple3.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 3);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'COUPLE', 4, 'Couple 4', 'Civil Engineering', '/images/couple4.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 4);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'COUPLE', 5, 'Couple 5', 'Electrical Engineering', '/images/couple5.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 5);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'COUPLE', 6, 'Couple 6', 'Information Technology', '/images/couple6.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 6);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'COUPLE', 7, 'Couple 7', 'Biotechnology', '/images/couple7.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 7);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'COUPLE', 8, 'Couple 8', 'Chemical Engineering', '/images/couple8.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 8);

INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
SELECT 'COUPLE', 9, 'Couple 9', 'Architecture', '/images/couple9.jpg', 0
WHERE NOT EXISTS (SELECT 1 FROM candidates WHERE category = 'COUPLE' AND candidate_number = 9);

-- Sample Voters (Optional - for testing)
-- Uncomment and modify as needed
-- INSERT INTO voters (pin, device_id, has_voted, created_at) 
-- SELECT '12345', 'device-12345', false, CURRENT_TIMESTAMP
-- WHERE NOT EXISTS (SELECT 1 FROM voters WHERE pin = '12345');
