-- Longer enum strings (e.g. PENDING_FIRST_APPROVAL) need a wider column than legacy VARCHAR(16).
ALTER TABLE expenses MODIFY COLUMN status VARCHAR(64) NULL;
