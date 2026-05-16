-- Contact form messages (CEO/ADMIN management)
CREATE TABLE IF NOT EXISTS contact_inquiries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    replied TINYINT(1) NOT NULL DEFAULT 0,
    reply_message TEXT NULL,
    created_at DATETIME(6) NULL,
    replied_at DATETIME(6) NULL
);

-- Account deletion workflow
CREATE TABLE IF NOT EXISTS delete_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reason TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by_id BIGINT NULL,
    review_note TEXT NULL,
    requested_at DATETIME(6) NULL,
    reviewed_at DATETIME(6) NULL
);
