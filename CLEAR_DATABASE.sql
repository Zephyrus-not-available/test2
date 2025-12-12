-- Script to clear old candidate data
-- Run this in your PostgreSQL database before restarting the application

-- Clear all existing candidates
DELETE FROM votes;
DELETE FROM candidates;

-- Reset sequences if needed
-- ALTER SEQUENCE candidates_id_seq RESTART WITH 1;

-- Optionally, clear voters (be careful with this in production!)
-- DELETE FROM voters;

