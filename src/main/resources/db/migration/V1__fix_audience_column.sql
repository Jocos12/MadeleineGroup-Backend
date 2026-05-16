-- Widen audience so all AnnouncementAudience enum names persist (fixes MySQL "Data truncated for column 'audience'").
-- Safe to run on existing tables: converts ENUM to VARCHAR(64) or extends VARCHAR.

ALTER TABLE announcements
    MODIFY COLUMN audience VARCHAR(64) NOT NULL;
