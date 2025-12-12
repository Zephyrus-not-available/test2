-- Verify candidate data
\echo 'Total candidates:'
SELECT COUNT(*) FROM candidates;

\echo '\nCandidates by category:'
SELECT category, COUNT(*) as count FROM candidates GROUP BY category ORDER BY category;

\echo '\nSample KING candidates:'
SELECT candidate_number, name, image_url FROM candidates WHERE category='KING' ORDER BY candidate_number;

\echo '\nSample PRINCE candidates:'
SELECT candidate_number, name, image_url FROM candidates WHERE category='PRINCE' ORDER BY candidate_number;

\echo '\nSample PRINCESS candidates:'
SELECT candidate_number, name, image_url FROM candidates WHERE category='PRINCESS' ORDER BY candidate_number;

