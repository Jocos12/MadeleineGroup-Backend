-- =============================================================================
-- Flyway: clear a FAILED migration from history (metadata only)
-- =============================================================================
-- Use when Spring Boot / Flyway reports:
--   "Detected failed migration to version 4 ..."
--
-- This DELETE only removes rows in flyway_schema_history where success = 0.
-- It does NOT drop the flyway_schema_history table and does NOT delete application data.
--
-- Preferred in automation:  mvn flyway:repair
--   (also fixes checksum mismatches if a migration file was edited after success)
--
-- After repair: restart the app so Flyway can run the migration again.
-- =============================================================================

DELETE FROM flyway_schema_history
WHERE success = 0
  AND version = '4';

-- If your failed version is not 4, adjust the version literal above, or run:
--   SELECT * FROM flyway_schema_history WHERE success = 0;
-- then delete those installed_rank rows as appropriate.
