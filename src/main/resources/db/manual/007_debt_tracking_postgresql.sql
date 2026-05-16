-- Debt tracking schema (PostgreSQL) — reference if you use Postgres instead of MySQL.
-- Adjust types to match your deployment.

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS amount_paid DECIMAL(10,2) DEFAULT 0;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS last_reminder_sent TIMESTAMP;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS debt_notes TEXT;

CREATE TABLE IF NOT EXISTS debt_payments (
  id BIGSERIAL PRIMARY KEY,
  booking_id BIGINT REFERENCES bookings(id),
  client_id BIGINT REFERENCES clients(id),
  amount DECIMAL(10,2) NOT NULL,
  payment_date DATE NOT NULL,
  payment_method VARCHAR(50),
  note TEXT,
  recorded_by BIGINT REFERENCES users(id),
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS debt_reminders (
  id BIGSERIAL PRIMARY KEY,
  booking_id BIGINT REFERENCES bookings(id),
  client_id BIGINT REFERENCES clients(id),
  sent_at TIMESTAMP DEFAULT NOW(),
  sent_by BIGINT REFERENCES users(id),
  method VARCHAR(20),
  message TEXT
);
