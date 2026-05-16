-- Two-level expense approval: first approver + final PAID approver
ALTER TABLE expenses
    ADD COLUMN first_approved_by_id BIGINT NULL,
    ADD COLUMN first_approved_at DATETIME(6) NULL;

ALTER TABLE expenses
    ADD CONSTRAINT fk_expenses_first_approved_by
        FOREIGN KEY (first_approved_by_id) REFERENCES users (id);

-- Legacy APPROVED stored the first approver on approved_by_id; move to first_approved_* then clear for second-step semantics.
UPDATE expenses
SET first_approved_by_id = approved_by_id,
    first_approved_at = COALESCE(approved_at, NOW(6)),
    approved_by_id = NULL,
    approved_at = NULL
WHERE status = 'APPROVED';

UPDATE expenses SET status = 'FIRST_APPROVED' WHERE status = 'APPROVED';
