-- Optional IP / user-agent for login audit rows (nullable for legacy rows)
ALTER TABLE login_audits
    ADD COLUMN ip_address VARCHAR(64) NULL,
    ADD COLUMN user_agent VARCHAR(512) NULL;
