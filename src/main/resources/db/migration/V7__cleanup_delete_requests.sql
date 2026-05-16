-- V7: Remove duplicate approved_by_id / approved_at on delete_requests
--
-- Domain decision (see DeleteRequest.java):
--   reviewed_by_id + reviewed_at + review_note are the single "decision" workflow
--   (CEO/admin approve or reject). approved_* were legacy/duplicate columns (e.g. older
--   Hibernate naming); setApprovedBy() in code only sets reviewedBy.
--
-- 1) Copy any values only stored on approved_* into reviewed_*
-- 2) Drop FK on approved_by_id if present
-- 3) Drop approved_by_id, approved_at

UPDATE delete_requests
SET reviewed_by_id = approved_by_id
WHERE reviewed_by_id IS NULL
  AND approved_by_id IS NOT NULL;

UPDATE delete_requests
SET reviewed_at = approved_at
WHERE reviewed_at IS NULL
  AND approved_at IS NOT NULL;

SET @fk := (
    SELECT kcu.CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE kcu
    WHERE kcu.TABLE_SCHEMA = DATABASE()
      AND kcu.TABLE_NAME = 'delete_requests'
      AND kcu.COLUMN_NAME = 'approved_by_id'
      AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @dropfk := IF(
    @fk IS NOT NULL,
    CONCAT('ALTER TABLE delete_requests DROP FOREIGN KEY `', @fk, '`'),
    'SELECT 1'
);
PREPARE stmt FROM @dropfk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE delete_requests DROP COLUMN approved_by_id;
ALTER TABLE delete_requests DROP COLUMN approved_at;
