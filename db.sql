-- =====================================================================
-- KTUVotingapp PostgreSQL bootstrap script
-- Run these steps in order to create the schema and seed starter data.
-- =====================================================================

-- 1) Create the application database (run as a superuser).
--    Adjust the owner/password as needed.
-- CREATE DATABASE ktuvoting WITH ENCODING 'UTF8';
-- CREATE USER ktuvote_user WITH PASSWORD 'change_me';
-- GRANT ALL PRIVILEGES ON DATABASE ktuvoting TO ktuvote_user;
-- \c ktuvoting

-- 2) Tables
-- Voters: tracks PIN/device binding and voting status
-- PIN can be shared across multiple devices (one PIN for all users)
-- Device ID is unique to prevent duplicate votes from same device
CREATE TABLE IF NOT EXISTS voters (
    id           BIGSERIAL PRIMARY KEY,
    pin          VARCHAR(5)  NOT NULL,
    device_id    VARCHAR(255) NOT NULL UNIQUE,
    has_voted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    voted_at     TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_pin       ON voters(pin);
CREATE INDEX IF NOT EXISTS idx_has_voted ON voters(has_voted);
CREATE INDEX IF NOT EXISTS idx_device_id ON voters(device_id);

-- Candidates: contestants grouped by category
CREATE TABLE IF NOT EXISTS candidates (
    id               BIGSERIAL PRIMARY KEY,
    category         VARCHAR(20)  NOT NULL,
    candidate_number INTEGER      NOT NULL,
    name             VARCHAR(100) NOT NULL,
    department       VARCHAR(100),
    image_url        VARCHAR(500),
    vote_count       BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_category_number UNIQUE (category, candidate_number)
);
CREATE INDEX IF NOT EXISTS idx_category_number ON candidates(category, candidate_number);

-- Votes: one vote per voter per category
CREATE TABLE IF NOT EXISTS votes (
    id           BIGSERIAL PRIMARY KEY,
    voter_id     BIGINT NOT NULL REFERENCES voters(id) ON DELETE RESTRICT,
    candidate_id BIGINT NOT NULL REFERENCES candidates(id) ON DELETE RESTRICT,
    category     VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_voter_category UNIQUE (voter_id, category)
);
CREATE INDEX IF NOT EXISTS idx_voter_id      ON votes(voter_id);
CREATE INDEX IF NOT EXISTS idx_candidate_id  ON votes(candidate_id);
CREATE INDEX IF NOT EXISTS idx_category      ON votes(category);
CREATE INDEX IF NOT EXISTS idx_voter_category ON votes(voter_id, category);

-- 3) Seed candidates (idempotent)
INSERT INTO candidates (category, candidate_number, name, department, image_url, vote_count)
VALUES
    ('KING', 1, 'Candidate King 1', 'Computer Science', '/images/king1.jpg', 0),
    ('KING', 2, 'Candidate King 2', 'Electronics', '/images/king2.jpg', 0),
    ('KING', 3, 'Candidate King 3', 'Mechanical', '/images/king3.jpg', 0),
    ('QUEEN', 1, 'Candidate Queen 1', 'Computer Science', '/images/queen1.jpg', 0),
    ('QUEEN', 2, 'Candidate Queen 2', 'Electronics', '/images/queen2.jpg', 0),
    ('QUEEN', 3, 'Candidate Queen 3', 'Mechanical', '/images/queen3.jpg', 0),
    ('PRINCE', 1, 'Candidate Prince 1', 'Computer Science', '/images/prince1.jpg', 0),
    ('PRINCE', 2, 'Candidate Prince 2', 'Electronics', '/images/prince2.jpg', 0),
    ('PRINCE', 3, 'Candidate Prince 3', 'Mechanical', '/images/prince3.jpg', 0),
    ('PRINCESS', 1, 'Candidate Princess 1', 'Computer Science', '/images/princess1.jpg', 0),
    ('PRINCESS', 2, 'Candidate Princess 2', 'Electronics', '/images/princess2.jpg', 0),
    ('PRINCESS', 3, 'Candidate Princess 3', 'Mechanical', '/images/princess3.jpg', 0),
    ('COUPLE', 1, 'Couple 1', 'Computer Science', '/images/couple1.jpg', 0),
    ('COUPLE', 2, 'Couple 2', 'Electronics', '/images/couple2.jpg', 0),
    ('COUPLE', 3, 'Couple 3', 'Mechanical', '/images/couple3.jpg', 0)
ON CONFLICT (category, candidate_number) DO NOTHING;

-- 4) Seed voters for testing (optional - for shared PIN system)
-- Create a dummy voter record with the shared PIN so PIN verification works
-- Multiple devices can use the same PIN - each will get its own voter record when voting
INSERT INTO voters (pin, device_id, has_voted)
SELECT '12345', 'shared-pin-seed-' || EXTRACT(EPOCH FROM NOW()), FALSE
WHERE NOT EXISTS (SELECT 1 FROM voters WHERE pin = '12345' LIMIT 1);

-- 5) Verify
-- SELECT * FROM candidates;
-- SELECT * FROM voters;
-- The application endpoints under /api should now read/write against this schema.

