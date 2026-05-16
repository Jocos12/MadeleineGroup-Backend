-- V5: Timestamp columns on delete_requests required by DeleteRequest (requestedAt, reviewedAt).
-- Hibernate may have created the table without these when ddl-auto was update; validate then fails.

ALTER TABLE delete_requests ADD COLUMN requested_at DATETIME(6) NULL;
ALTER TABLE delete_requests ADD COLUMN reviewed_at DATETIME(6) NULL;
