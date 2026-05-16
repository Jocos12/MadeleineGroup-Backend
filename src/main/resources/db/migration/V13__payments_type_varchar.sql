-- Store payment type as string (VARCHAR), not MySQL ENUM, to match JPA @JdbcTypeCode(VARCHAR).
ALTER TABLE payments
    MODIFY COLUMN type VARCHAR(50) NOT NULL;
