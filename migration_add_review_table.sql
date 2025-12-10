-- Migration script to add review table
-- Run this if you have an existing database without the review table

CREATE TABLE IF NOT EXISTS review (
    review_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    place_id        UUID NOT NULL REFERENCES place(place_id) ON DELETE CASCADE,
    rating          INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment         TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, place_id)
);

CREATE INDEX IF NOT EXISTS idx_review_user ON review(user_id);
CREATE INDEX IF NOT EXISTS idx_review_place ON review(place_id);
CREATE INDEX IF NOT EXISTS idx_review_place_created ON review(place_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_review_rating ON review(place_id, rating);

