-- Migration script to add profile_image column to users table
-- Run this if you have an existing database without the profile_image column

ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image TEXT;

