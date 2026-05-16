-- V4: Backfill requested_by_id on delete_requests (data fix only)
--
-- Prerequisites (already applied in this environment via Hibernate):
--   - delete_requests.requested_by_id exists
--   - contact_inquiries.phone exists
--   - FK to the user to delete is stored as user_to_delete_id (not user_id)
--
-- No ADD COLUMN: avoids duplicate-column errors when Hibernate already created the columns.

UPDATE delete_requests dr
INNER JOIN users u ON dr.user_to_delete_id = u.id
SET dr.requested_by_id = u.id
WHERE dr.user_to_delete_id IS NOT NULL
  AND dr.requested_by_id IS NULL;

ALTER TABLE delete_requests MODIFY COLUMN requested_by_id BIGINT NOT NULL;
