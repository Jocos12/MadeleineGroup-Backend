-- Number of recipients selected for notification email when CEO checks "send email" (for dashboard display).
ALTER TABLE announcements
    ADD COLUMN email_notified_count INT NOT NULL DEFAULT 0;
