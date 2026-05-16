-- Debt tracking schema (MySQL 8+).
-- JPA maps Booking.paidAmount -> amount_paid (see Booking.java). If an older DB still has `paid_amount`, run:
--   ALTER TABLE bookings CHANGE COLUMN paid_amount amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0;
-- before relying on this script, or drop paid_amount after copying data — avoid having both columns.

-- ---------------------------------------------------------------------------
-- bookings: extra columns (IF NOT EXISTS requires MySQL 8.0.12+)
-- ---------------------------------------------------------------------------
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS amount_paid DECIMAL(10,2) DEFAULT 0;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) NULL;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS last_reminder_sent DATETIME(6) NULL;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS debt_notes TEXT NULL;

-- ---------------------------------------------------------------------------
-- debt_payments: partial payments / debt history per booking
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS debt_payments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  booking_id BIGINT NOT NULL,
  client_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_date DATE NOT NULL,
  payment_method VARCHAR(50) NULL,
  note TEXT NULL,
  recorded_by BIGINT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_debt_payments_booking (booking_id),
  KEY idx_debt_payments_client (client_id),
  CONSTRAINT fk_debt_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id),
  CONSTRAINT fk_debt_payments_client FOREIGN KEY (client_id) REFERENCES clients (id),
  CONSTRAINT fk_debt_payments_user FOREIGN KEY (recorded_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- debt_reminders: reminder sends log
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS debt_reminders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  booking_id BIGINT NOT NULL,
  client_id BIGINT NOT NULL,
  sent_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  sent_by BIGINT NULL,
  method VARCHAR(20) NULL,
  message TEXT NULL,
  PRIMARY KEY (id),
  KEY idx_debt_reminders_booking (booking_id),
  KEY idx_debt_reminders_client (client_id),
  CONSTRAINT fk_debt_reminders_booking FOREIGN KEY (booking_id) REFERENCES bookings (id),
  CONSTRAINT fk_debt_reminders_client FOREIGN KEY (client_id) REFERENCES clients (id),
  CONSTRAINT fk_debt_reminders_user FOREIGN KEY (sent_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
