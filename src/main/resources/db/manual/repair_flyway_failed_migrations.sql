-- Run manually in MySQL when Flyway reports a failed migration (e.g. validate error on version 12).
-- Then restart the app (use spring.flyway.repair-on-migrate=true once if checksum mismatch after editing V12).

DELETE FROM flyway_schema_history WHERE success = 0;
-- Or only version 12:
-- DELETE FROM flyway_schema_history WHERE version = '12' AND success = 0;
