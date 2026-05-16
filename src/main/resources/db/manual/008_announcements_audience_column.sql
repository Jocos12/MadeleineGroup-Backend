-- Fix: Data truncated for column 'audience' (MySQL ENUM or VARCHAR too small for values like ALL_TEAM, EVERYONE, etc.)
-- Run once on your DB after deploying new AnnouncementAudience enum values.

-- MySQL / MariaDB
ALTER TABLE announcements MODIFY COLUMN audience VARCHAR(64) NOT NULL;

-- Optional: if Hibernate ddl-auto did not add image_urls (announcement images)
-- ALTER TABLE announcements ADD COLUMN image_urls JSON NULL;

-- PostgreSQL (uncomment if you use PostgreSQL instead)
-- ALTER TABLE announcements ALTER COLUMN audience TYPE VARCHAR(64);
