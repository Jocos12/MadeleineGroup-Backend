package rw.madeleinegroup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rw.madeleinegroup.entity.Expense;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.logo.url:}")
    private String appLogoUrl;
    @Value("${app.contact.phone:}")
    private String contactPhone;
    @Value("${app.contact.email:}")
    private String contactEmail;
    @Value("${app.api-base-url:http://localhost:8082}")
    private String apiBaseUrl;
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        if (to == null || to.isBlank()) return;
        try {
            String body = "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:#475569;\">Your one-time password is:</p>"
                + "<div style=\"background:#ecfdf5;border-left:4px solid #2d9e5f;padding:20px;border-radius:12px;margin-bottom:20px;\">"
                + "<p style=\"margin:0;font-size:28px;font-weight:700;letter-spacing:0.2em;color:#1a5c38;\">" + escapeHtml(otp) + "</p></div>"
                + "<p style=\"margin:0 0 8px;font-size:14px;color:#64748b;\">This OTP expires in 10 minutes.</p>"
                + "<p style=\"margin:0;font-size:14px;color:#64748b;\">Do not share this code with anyone.</p>";
            String html = buildBrandedEmailWrapper("CODE DE CONNEXION / LOGIN OTP", body);
            sendHtmlEmail(to, "Madeleine Group - Your Login OTP", html);
            log.info("OTP email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Branded HTML alert when an expense exceeds the auto-approve threshold (same visual language as OTP emails).
     */
    @Async
    public void sendExpenseApprovalRequiredEmail(String to, Expense expense, String amountPlain,
                                                 String description, String branchName, String statusLabel) {
        if (to == null || to.isBlank() || expense == null) {
            return;
        }
        try {
            String idStr = String.valueOf(expense.getId());
            String amt = escapeHtml(amountPlain != null ? amountPlain : "?");
            String desc = escapeHtml(description != null ? description : "—");
            String branch = escapeHtml(branchName != null ? branchName : "—");
            String status = escapeHtml(statusLabel != null ? statusLabel : "—");
            String limit = escapeHtml(Expense.CEO_AUTO_APPROVE_MAX_RWF.toPlainString());

            String dash = (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl.trim().replaceAll("/+$", "") : "http://localhost:3000";
            String ctaHref = dash + "/dashboard";

            String body = "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:#475569;\">"
                    + "Une dépense dépasse la limite d'auto-approbation et nécessite votre validation.<br/>"
                    + "<span style=\"color:#64748b;\">An expense exceeds the auto-approve limit and requires your approval.</span>"
                    + "</p>"
                    + "<div style=\"background:#f0fdf4;border:1px solid #bbf7d0;border-radius:12px;padding:22px 20px;margin:0 0 22px;text-align:center;\">"
                    + "<p style=\"margin:0 0 6px;font-size:13px;font-weight:600;color:#64748b;text-transform:uppercase;letter-spacing:0.06em;\">Montant / Amount (RWF)</p>"
                    + "<p style=\"margin:0;font-size:30px;font-weight:800;letter-spacing:0.04em;color:#dc2626;\">" + amt + " <span style=\"font-size:18px;color:#64748b;font-weight:600;\">RWF</span></p>"
                    + "</div>"
                    + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:14px;color:#334155;margin-bottom:20px;\">"
                    + "<tr><td style=\"padding:10px 0;border-bottom:1px solid #e5e7eb;color:#64748b;width:40%;\">ID</td><td style=\"padding:10px 0;border-bottom:1px solid #e5e7eb;font-weight:600;\">#" + escapeHtml(idStr) + "</td></tr>"
                    + "<tr><td style=\"padding:10px 0;border-bottom:1px solid #e5e7eb;color:#64748b;\">Branche / Branch</td><td style=\"padding:10px 0;border-bottom:1px solid #e5e7eb;font-weight:600;\">" + branch + "</td></tr>"
                    + "<tr><td style=\"padding:10px 0;border-bottom:1px solid #e5e7eb;color:#64748b;\">Statut / Status</td><td style=\"padding:10px 0;border-bottom:1px solid #e5e7eb;\"><span style=\"display:inline-block;background:#fef3c7;color:#92400e;padding:4px 12px;border-radius:999px;font-size:12px;font-weight:700;\">" + status + "</span></td></tr>"
                    + "<tr><td style=\"padding:10px 0;color:#64748b;vertical-align:top;\">Description</td><td style=\"padding:10px 0;font-weight:500;\">" + desc + "</td></tr>"
                    + "<tr><td style=\"padding:10px 0 0;color:#64748b;\">Auto-approve limit</td><td style=\"padding:10px 0 0;font-weight:600;color:#1a5c38;\">" + limit + " RWF</td></tr>"
                    + "</table>"
                    + "<p style=\"margin:0 0 20px;font-size:13px;line-height:1.6;color:#64748b;\">"
                    + "Connectez-vous au tableau de bord pour approuver ou rejeter cette dépense.<br/>"
                    + "Sign in to the dashboard to approve or reject this expense."
                    + "</p>"
                    + "<p style=\"text-align:center;margin:0;\"><a href=\"" + escapeHtml(ctaHref) + "\" style=\"display:inline-block;background:#f0b429;color:#1a2937 !important;text-decoration:none;border-radius:8px;padding:14px 28px;font-size:15px;font-weight:700;\">"
                    + "Ouvrir le tableau de bord / Open dashboard →"
                    + "</a></p>";

            String html = buildBrandedEmailWrapper("APPROBATION DE DÉPENSE / EXPENSE APPROVAL", body);
            sendHtmlEmail(to.trim(), "Madeleine Group — Expense approval required / Approbation de dépense", html);
            log.info("Expense approval HTML email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send expense approval email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a branded HTML announcement (round logo + teal header) to each recipient.
     */
    /**
     * Sends branded HTML announcement emails. When {@code imageUrls} is non-empty, each public URL is embedded
     * as an inline {@code <img>} (first image prominent, rest as thumbnails). Relative paths are resolved with
     * {@code app.api-base-url}. For reliable display in Gmail/Outlook, production should use https URLs only.
     */
    @Async
    public void sendAnnouncementBrandedEmails(List<String> recipientEmails, String title,
                                                String bodyPlain, String authorLabel,
                                                List<String> imageUrls, String audienceLabel) {
        if (recipientEmails == null || recipientEmails.isEmpty()) {
            return;
        }
        String author = authorLabel != null && !authorLabel.isBlank() ? authorLabel.trim() : "Madeleine Group";
        String safeTitle = title != null && !title.isBlank() ? title.trim() : "Announcement";
        String subject = "Madeleine Group — " + (safeTitle.length() > 90 ? safeTitle.substring(0, 87) + "…" : safeTitle);
        for (String to : recipientEmails.stream().distinct().toList()) {
            if (to == null || to.isBlank()) {
                continue;
            }
            try {
                String inner = buildAnnouncementEmailInner(safeTitle, bodyPlain, author, imageUrls, audienceLabel);
                String html = buildBrandedEmailWrapper("NOUVELLE ANNONCE / NEW ANNOUNCEMENT", inner);
                sendHtmlEmail(to.trim(), subject, html);
            } catch (Exception e) {
                log.error("Announcement email failed for {}: {}", to, e.getMessage());
            }
        }
    }

    private String buildAnnouncementEmailInner(String title, String bodyPlain, String author,
                                               List<String> imageUrls, String audienceLabel) {
        String bodyHtml = plainToHtml(bodyPlain != null ? bodyPlain : "");
        StringBuilder inner = new StringBuilder();
        inner.append("<p style=\"margin:0 0 12px;font-size:14px;color:#64748b;\">")
                .append("From <strong style=\"color:#0d6e6e;\">").append(escapeHtml(author)).append("</strong></p>");
        if (audienceLabel != null && !audienceLabel.isBlank()) {
            inner.append("<p style=\"margin:0 0 14px;\"><span style=\"display:inline-block;background:#e8f4f4;color:#0d6e6e;padding:6px 14px;border-radius:999px;font-size:11px;font-weight:700;letter-spacing:0.04em;text-transform:uppercase;\">")
                    .append(escapeHtml(audienceLabel.trim()))
                    .append("</span></p>");
        }
        inner.append("<h2 style=\"margin:0 0 18px;font-size:20px;color:#1e293b;line-height:1.35;\">")
                .append(escapeHtml(title))
                .append("</h2>")
                .append(bodyHtml)
                .append(buildAnnouncementImagesSection(imageUrls));

        String dash = (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl.trim().replaceAll("/+$", "") : "http://localhost:3000";
        String ctaHref = dash + "/dashboard";
        inner.append("<p style=\"text-align:center;margin:28px 0 0;\"><a href=\"")
                .append(escapeHtml(ctaHref))
                .append("\" style=\"display:inline-block;background:#0d6e6e;color:#ffffff !important;text-decoration:none;border-radius:25px;padding:14px 32px;font-size:15px;font-weight:600;\">")
                .append("View in dashboard → / Voir le tableau de bord →")
                .append("</a></p>");
        inner.append("<p style=\"text-align:center;margin:12px 0 0;font-size:12px;color:#94a3b8;line-height:1.5;\">")
                .append("Open the Madeleine Group dashboard for the full announcement.")
                .append("</p>");
        return inner.toString();
    }

    /**
     * Builds a table-based image block (better Outlook support than flex/grid). Skips unsafe or unresolvable URLs.
     */
    private String buildAnnouncementImagesSection(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return "";
        }
        List<String> absolute = new ArrayList<>();
        for (String raw : imageUrls) {
            String n = normalizeAnnouncementImageUrl(raw);
            if (n != null) {
                absolute.add(n);
            }
        }
        if (absolute.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"margin:28px 0 0;\">");
        sb.append("<p style=\"margin:0 0 12px;font-size:12px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:0.06em;\">")
                .append("Images / Images")
                .append("</p>");

        String mainSrc = escapeHtml(absolute.get(0));
        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 14px;\"><tr><td align=\"center\">");
        sb.append("<img src=\"").append(mainSrc).append("\" alt=\"Announcement\" width=\"520\" ")
                .append("style=\"display:block;width:100%;max-width:520px;max-height:300px;height:auto;object-fit:cover;border-radius:12px;border:0;outline:none;text-decoration:none;\" />");
        sb.append("</td></tr></table>");

        if (absolute.size() > 1) {
            sb.append("<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:0;\"><tr>");
            for (int i = 1; i < absolute.size(); i++) {
                String src = escapeHtml(absolute.get(i));
                sb.append("<td style=\"padding:4px 6px 4px 0;vertical-align:top;\">");
                sb.append("<img src=\"").append(src).append("\" alt=\"\" width=\"120\" height=\"90\" ")
                        .append("style=\"display:block;width:120px;height:90px;object-fit:cover;border-radius:8px;border:2px solid #e2e8f0;\" />");
                sb.append("</td>");
            }
            sb.append("</tr></table>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    /** Returns an absolute http(s) URL safe for {@code img src}, or null if the value must be skipped. */
    private String normalizeAnnouncementImageUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String u = raw.trim();
        if (u.isEmpty()) {
            return null;
        }
        String lower = u.toLowerCase(Locale.ROOT);
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")) {
            return null;
        }
        if (lower.startsWith("https://") || lower.startsWith("http://")) {
            return u;
        }
        if (u.startsWith("//")) {
            return "https:" + u;
        }
        String base = (apiBaseUrl != null && !apiBaseUrl.isBlank()) ? apiBaseUrl.trim().replaceAll("/+$", "") : "http://localhost:8082";
        if (u.startsWith("/")) {
            return base + u;
        }
        return base + "/" + u;
    }

    @Async
    public void sendNotificationEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send notification email: {}", e.getMessage());
        }
    }

    /**
     * Sends the debt reminder email from the Debts dashboard (branded HTML). Synchronous — throws on SMTP failure.
     * {@code lang} should be en, fr, or rw (case-insensitive).
     */
    public void sendDebtReminderFromDashboardSync(String toEmail, String lang, String messageBody) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Client has no email address");
        }
        String body = messageBody != null ? messageBody.trim() : "";
        if (body.isEmpty()) {
            throw new IllegalArgumentException("Message body is required");
        }
        String l = lang != null ? lang.trim().toLowerCase(java.util.Locale.ROOT) : "en";
        String headerTitle;
        String subject;
        switch (l) {
            case "fr" -> {
                headerTitle = "RAPPEL DE PAIEMENT";
                subject = "Madeleine Group — Rappel de paiement";
            }
            case "rw" -> {
                headerTitle = "KWIBUKA KWISHYURA";
                subject = "Madeleine Group — Kwibuka kwishyura";
            }
            default -> {
                headerTitle = "PAYMENT REMINDER";
                subject = "Madeleine Group — Payment reminder";
            }
        }
        String inner = plainToHtml(body);
        String html = buildBrandedEmailWrapper(headerTitle, inner);
        sendHtmlEmailSync(toEmail.trim(), subject, html);
    }

    /** Sends reminder email synchronously as plain text; throws on failure so caller can count success/failure. */
    public void sendReminderEmailSync(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Reminder email sent to {}", to);
    }

    /** Sends reminder email as HTML with premium template. Logo via app.logo.url; language FR or EN. */
    public void sendReminderEmailHtmlSync(String to, String subject, String clientName, String bookingRef,
                                          String amountRwf, String eventDateStr, String bodyPlain, String language) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, java.nio.charset.StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            String html = buildReminderEmailHtml(clientName, bookingRef, amountRwf, eventDateStr, bodyPlain, "FR".equalsIgnoreCase(language) ? "FR" : "EN");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Reminder HTML email sent to {}", to);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reminder email: " + e.getMessage(), e);
        }
    }

    private String buildReminderEmailHtml(String clientName, String bookingRef, String amountRwf, String eventDateStr, String bodyPlain, String lang) {
        boolean isFr = "FR".equals(lang);
        String logoTag = buildLogoImgTag();
        String headerTitle = isFr ? "RAPPEL DE PAIEMENT" : "PAYMENT REMINDER";
        String greeting = isFr ? ("Bonjour <strong>" + escapeHtml(clientName) + "</strong>,") : ("Dear <strong>" + escapeHtml(clientName) + "</strong>,");
        String labelRef = isFr ? "Référence" : "Reference";
        String labelAmount = isFr ? "Montant dû" : "Amount due";
        String labelDate = isFr ? "Date de l'événement" : "Event date";
        String ctaText = isFr ? "Nous Contacter" : "Contact Us";
        String contactIntro = isFr ? "Contact Madeleine Group :" : "Contact Madeleine Group:";
        String tagline = isFr ? "Événements premium · Rwanda" : "Premium events · Rwanda";
        String phone = (contactPhone != null && !contactPhone.isBlank()) ? contactPhone.trim() : "";
        String email = (contactEmail != null && !contactEmail.isBlank()) ? contactEmail.trim() : "";

        String bodyHtml = plainToHtml(bodyPlain);
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>Madeleine Group</title></head>");
        html.append("<body style=\"margin:0;font-family:'Segoe UI',Arial,sans-serif;background:#f4f7f6;\">");
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f7f6;padding:24px 16px;\"><tr><td align=\"center\">");
        html.append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background:#fff;border-radius:8px;box-shadow:0 4px 12px rgba(0,0,0,0.08);overflow:hidden;\">");
        html.append("<tr><td style=\"background:#0d6e6e;padding:30px;text-align:center;\">");
        html.append("<div style=\"margin-bottom:12px;\">").append(logoTag).append("</div>");
        html.append("<p style=\"margin:0;font-size:16px;font-weight:700;color:#fff;letter-spacing:0.08em;\">").append(headerTitle).append("</p>");
        html.append("</td></tr>");
        html.append("<tr><td style=\"padding:40px;color:#1e293b;\">");
        html.append("<p style=\"margin:0 0 20px;font-size:16px;font-weight:600;color:#1e293b;\">").append(greeting).append("</p>");
        html.append("<div style=\"background:#e8f4f4;border-left:4px solid #0d6e6e;padding:20px;border-radius:4px;margin-bottom:24px;\">");
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
        html.append("<tr><td style=\"padding:6px 0;font-size:14px;color:#64748b;width:45%;\">").append(labelRef).append("</td><td style=\"padding:6px 0;font-size:14px;font-weight:600;color:#1e293b;\">").append(escapeHtml(bookingRef)).append("</td></tr>");
        html.append("<tr><td style=\"padding:6px 0;font-size:14px;color:#64748b;\">").append(labelAmount).append("</td><td style=\"padding:6px 0;font-size:14px;font-weight:600;color:#1e293b;\">").append(escapeHtml(amountRwf != null ? amountRwf : "")).append(" <span style=\"color:#0d6e6e;\">RWF</span></td></tr>");
        html.append("<tr><td style=\"padding:6px 0;font-size:14px;color:#64748b;\">").append(labelDate).append("</td><td style=\"padding:6px 0;font-size:14px;font-weight:600;color:#1e293b;\">").append(escapeHtml(eventDateStr != null ? eventDateStr : "")).append("</td></tr>");
        html.append("</table></div>");
        html.append(bodyHtml);
        html.append("<p style=\"text-align:center;margin:28px 0 16px;\"><a href=\"mailto:").append(escapeHtml(email)).append("\" style=\"display:inline-block;background:#0d6e6e;color:#fff;text-decoration:none;border-radius:25px;padding:14px 40px;font-size:16px;font-weight:600;\">").append(ctaText).append("</a></p>");
        html.append("<p style=\"text-align:center;margin:0;font-size:13px;color:#64748b;\">").append(contactIntro).append(" ");
        if (!phone.isEmpty()) html.append(escapeHtml(phone));
        if (!phone.isEmpty() && !email.isEmpty()) html.append(" &middot; ");
        if (!email.isEmpty()) html.append("<a href=\"mailto:").append(escapeHtml(email)).append("\" style=\"color:#0d6e6e;\">").append(escapeHtml(email)).append("</a>");
        html.append("</p>");
        html.append("</td></tr>");
        html.append("<tr><td style=\"background:#f8f9fa;padding:20px;text-align:center;\">");
        html.append("<p style=\"margin:0;font-size:12px;color:#94a3b8;\">© Madeleine Group — ").append(tagline).append("</p>");
        html.append("</td></tr>");
        html.append("</table></td></tr></table></body></html>");
        return html.toString();
    }

    /** Shared wrapper for all branded emails: logo (round), header, body, footer. */
    private String buildBrandedEmailWrapper(String headerTitle, String bodyContent) {
        return buildBrandedEmailWrapper(headerTitle, bodyContent, "#1a5c38");
    }

    private String buildBrandedEmailWrapper(String headerTitle, String bodyContent, String headerBgHex) {
        String logoTag = buildLogoImgTag();
        String phone = (contactPhone != null && !contactPhone.isBlank()) ? contactPhone.trim() : "";
        String email = (contactEmail != null && !contactEmail.isBlank()) ? contactEmail.trim() : "";
        String headerBg = (headerBgHex != null && !headerBgHex.isBlank()) ? headerBgHex : "#1a5c38";
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>Madeleine Group</title></head>");
        html.append("<body style=\"margin:0;font-family:'Segoe UI',Arial,sans-serif;background:#f9fafb;\">");
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f9fafb;padding:24px 16px;\"><tr><td align=\"center\">");
        html.append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background:#ffffff;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);overflow:hidden;\">");
        html.append("<tr><td style=\"background:").append(escapeHtml(headerBg)).append(";padding:30px;text-align:center;\">");
        html.append("<div style=\"margin-bottom:12px;\">").append(logoTag).append("</div>");
        html.append("<p style=\"margin:0;font-size:16px;font-weight:700;color:#fff;letter-spacing:0.06em;\">").append(escapeHtml(headerTitle)).append("</p>");
        html.append("</td></tr>");
        html.append("<tr><td style=\"padding:40px;color:#1e293b;\">").append(bodyContent).append("</td></tr>");
        html.append("<tr><td style=\"background:#f8f9fa;padding:20px;text-align:center;\">");
        html.append("<p style=\"margin:0;font-size:12px;color:#94a3b8;\">© Madeleine Group — Événements premium · Rwanda</p>");
        if (!phone.isEmpty() || !email.isEmpty()) {
            html.append("<p style=\"margin:8px 0 0;font-size:12px;color:#94a3b8;\">");
            if (!phone.isEmpty()) html.append(escapeHtml(phone));
            if (!phone.isEmpty() && !email.isEmpty()) html.append(" · ");
            if (!email.isEmpty()) html.append("<a href=\"mailto:").append(escapeHtml(email)).append("\" style=\"color:#2d9e5f;\">").append(escapeHtml(email)).append("</a>");
            html.append("</p>");
        }
        html.append("</td></tr></table></td></tr></table></body></html>");
        return html.toString();
    }

    /** Logo in a circular container, no white gaps (object-fit:cover fills circle). Used in all branded emails. */
    private String buildLogoImgTag() {
        String url = (appLogoUrl != null && !appLogoUrl.isBlank()) ? appLogoUrl.trim() : "";
        if (url.isEmpty()) {
            return "<span style=\"font-size:22px;font-weight:700;letter-spacing:0.03em;color:#fff;\">Madeleine Group</span>";
        }
        return "<div style=\"border-radius:50%;overflow:hidden;width:100px;height:100px;margin:0 auto 15px auto;background:#0d6e6e;display:flex;align-items:center;justify-content:center;\">"
            + "<img src=\"" + escapeHtml(url) + "\" alt=\"Madeleine Group\" border=\"0\" width=\"100\" height=\"100\" style=\"object-fit:cover;border-radius:50%;display:block;\" />"
            + "</div>";
    }

    private static String plainToHtml(String plain) {
        if (plain == null || plain.isBlank()) return "<p style=\"font-size:15px;line-height:1.7;color:#475569;margin:0;\"></p>";
        String escaped = escapeHtml(plain);
        String style = "font-size:15px;line-height:1.7;color:#475569;margin:0 0 14px;";
        return "<p style=\"" + style + "\">"
            + escaped.replace("\n\n", "</p><p style=\"" + style + "\">")
                    .replace("\n", "<br/>")
            + "</p>";
    }

    /** Bilingual welcome email when a new user is created (with temporary password). Branded HTML with round logo. */
    @Async
    public void sendWelcomeEmail(String to, String fullName, String createdByName, String dateStr,
                                 String email, String temporaryPassword) {
        if (to == null || to.isBlank()) return;
        try {
            String name = fullName != null ? fullName : "User";
            String body = "<p style=\"margin:0 0 16px;font-size:16px;font-weight:600;color:#1e293b;\">Dear " + escapeHtml(name) + ",</p>"
                + "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:#475569;\">Welcome to the Madeleine Group family! Your account has been created by " + escapeHtml(createdByName != null ? createdByName : "an administrator") + " on " + escapeHtml(dateStr != null ? dateStr : "") + ".</p>"
                + "<div style=\"background:#e8f4f4;border-left:4px solid #0d6e6e;padding:20px;border-radius:4px;margin:20px 0;\">"
                + "<p style=\"margin:0 0 8px;font-size:14px;color:#64748b;\">Email</p><p style=\"margin:0 0 12px;font-size:15px;font-weight:600;color:#1e293b;\">" + escapeHtml(email != null ? email : "") + "</p>"
                + "<p style=\"margin:0 0 8px;font-size:14px;color:#64748b;\">Temporary password</p><p style=\"margin:0;font-size:15px;font-weight:600;color:#0d6e6e;\">" + escapeHtml(temporaryPassword != null ? temporaryPassword : "") + "</p>"
                + "</div>"
                + "<p style=\"margin:0 0 16px;font-size:14px;color:#64748b;\">Please log in at <a href=\"https://app.madeleinegroup.rw\" style=\"color:#0d6e6e;\">app.madeleinegroup.rw</a> and change your password after your first login.</p>"
                + "<p style=\"margin:0;font-size:14px;color:#475569;\">Warm regards,<br><strong style=\"color:#0d6e6e;\">The Madeleine Group Team</strong></p>";
            String html = buildBrandedEmailWrapper("BIENVENUE / WELCOME", body);
            sendHtmlEmail(to, "Madeleine Group - Welcome | Bienvenue", html);
            log.info("Welcome email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendBilingualBookingSubmissionEmail(String to, String clientName, String ref, String eventDate,
                                                    String eventType, int guestCount, String packagesText,
                                                    String totalRwf) {
        String en = "Dear " + clientName + ",\n\n" +
                "Thank you for your booking with Madeleine Group!\n\n" +
                "Your booking reference: " + ref + "\n" +
                "Event date: " + eventDate + "\n" +
                "Event type: " + eventType + "\n" +
                "Number of guests: " + guestCount + "\n\n" +
                "Selected services:\n" + packagesText + "\n" +
                "Total estimated amount: " + totalRwf + " RWF\n\n" +
                "The Madeleine team will contact you shortly to confirm and finalize the details. " +
                "For any questions, please reach us at our official Madeleine Group contact.\n\n" +
                "Warm regards,\nThe Madeleine Group Team";

        String fr = "Cher/Chere " + clientName + ",\n\n" +
                "Merci pour votre réservation chez Madeleine Group!\n\n" +
                "Votre référence: " + ref + "\n" +
                "Date de l'événement: " + eventDate + "\n" +
                "Type d'événement: " + eventType + "\n" +
                "Nombre d'invités: " + guestCount + "\n\n" +
                "Services sélectionnés:\n" + packagesText + "\n" +
                "Montant total estimé: " + totalRwf + " RWF\n\n" +
                "L'équipe Madeleine vous contactera bientôt pour confirmer et finaliser les détails. " +
                "Pour toute question, contactez-nous aux coordonnées officielles de Madeleine Group.\n\n" +
                "Cordialement,\nL'équipe Madeleine Group";

        sendNotificationEmail(to, "Madeleine Group - Booking Received / Réservation reçue", en + "\n\n---\n\n" + fr);
    }

    @Async
    public void sendBilingualBookingConfirmedEmail(String to, String clientName, String ref, String eventDate,
                                                   String eventType, int guestCount, String packagesText,
                                                   String totalRwf) {
        String en = "Dear " + clientName + ",\n\n" +
                "We are delighted to confirm your booking with Madeleine Group!\n\n" +
                "Your booking reference: " + ref + "\n" +
                "Event date: " + eventDate + "\n" +
                "Event type: " + eventType + "\n" +
                "Number of guests: " + guestCount + "\n\n" +
                "Confirmed services:\n" + packagesText + "\n" +
                "Total amount: " + totalRwf + " RWF\n\n" +
                "Your event is officially confirmed. Welcome to the Madeleine family! " +
                "For any questions before your event, please contact us at our official Madeleine Group details.\n\n" +
                "Warm regards,\nThe Madeleine Group Team";

        String fr = "Cher/Chere " + clientName + ",\n\n" +
                "Nous sommes ravis de confirmer votre réservation chez Madeleine Group!\n\n" +
                "Votre référence: " + ref + "\n" +
                "Date de l'événement: " + eventDate + "\n" +
                "Type d'événement: " + eventType + "\n" +
                "Nombre d'invités: " + guestCount + "\n\n" +
                "Services confirmés:\n" + packagesText + "\n" +
                "Montant total: " + totalRwf + " RWF\n\n" +
                "Votre événement est officiellement confirmé. Bienvenue dans la famille Madeleine! " +
                "Pour toute question avant votre événement, contactez-nous aux coordonnées officielles de Madeleine Group.\n\n" +
                "Cordialement,\nL'équipe Madeleine Group";

        sendNotificationEmail(to, "Madeleine Group - Booking Confirmed / Réservation confirmée", en + "\n\n---\n\n" + fr);
    }

    @Async
    public void sendInternalNewBookingAlert(String adminEmail, String clientName, String ref, String eventDate,
                                            String packagesSummary, String totalRwf) {
        String body = "New booking arrived:\n\n" +
                "Client: " + clientName + "\n" +
                "Reference: " + ref + "\n" +
                "Date: " + eventDate + "\n" +
                "Packages: " + packagesSummary + "\n" +
                "Total: " + totalRwf + " RWF\n\n" +
                "Please review and confirm in the admin dashboard.";
        sendNotificationEmail(adminEmail, "Madeleine Group - New Booking Alert", body);
    }

    /** Sends bilingual (EN/FR) email to client when booking status changes. Includes modifier name and date. */
    @Async
    public void sendBilingualBookingStatusChangedEmail(String to, String clientName, String ref, String eventDate,
                                                      String eventType, int guestCount, String packagesText,
                                                      String totalRwf, String newStatus, String modifiedBy,
                                                      String modifiedDate) {
        String statusEn = translateStatusEn(newStatus);
        String statusFr = translateStatusFr(newStatus);

        String en = "Dear " + clientName + ",\n\n" +
                "Your booking status with Madeleine Group has been updated.\n\n" +
                "Booking reference: " + ref + "\n" +
                "Event date: " + eventDate + "\n" +
                "Event type: " + eventType + "\n" +
                "Number of guests: " + guestCount + "\n\n" +
                "New status: " + statusEn + "\n" +
                (modifiedBy != null && !modifiedBy.isBlank() ? "Updated by: " + modifiedBy + "\n" : "") +
                (modifiedDate != null && !modifiedDate.isBlank() ? "Date: " + modifiedDate + "\n" : "") +
                "\nSelected services:\n" + (packagesText != null ? packagesText : "None") + "\n" +
                "Total estimated amount: " + (totalRwf != null ? totalRwf : "0") + " RWF\n\n" +
                "For any questions, please contact us at our official Madeleine Group details.\n\n" +
                "Warm regards,\nThe Madeleine Group Team";

        String fr = "Cher/Chere " + clientName + ",\n\n" +
                "Le statut de votre réservation chez Madeleine Group a été mis à jour.\n\n" +
                "Référence: " + ref + "\n" +
                "Date de l'événement: " + eventDate + "\n" +
                "Type d'événement: " + eventType + "\n" +
                "Nombre d'invités: " + guestCount + "\n\n" +
                "Nouveau statut: " + statusFr + "\n" +
                (modifiedBy != null && !modifiedBy.isBlank() ? "Modifié par: " + modifiedBy + "\n" : "") +
                (modifiedDate != null && !modifiedDate.isBlank() ? "Date: " + modifiedDate + "\n" : "") +
                "\nServices sélectionnés:\n" + (packagesText != null ? packagesText : "Aucun") + "\n" +
                "Montant total estimé: " + (totalRwf != null ? totalRwf : "0") + " RWF\n\n" +
                "Pour toute question, contactez-nous aux coordonnées officielles de Madeleine Group.\n\n" +
                "Cordialement,\nL'équipe Madeleine Group";

        sendNotificationEmail(to, "Madeleine Group - Status Update / Mise à jour du statut", en + "\n\n---\n\n" + fr);
    }

    private static String translateStatusEn(String status) {
        if (status == null) return "Updated";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Pending";
            case "CONFIRMED" -> "Confirmed";
            case "IN_PROGRESS" -> "In Progress";
            case "COMPLETED" -> "Completed";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }

    private static String translateStatusFr(String status) {
        if (status == null) return "Mis à jour";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "En attente";
            case "CONFIRMED" -> "Confirmé";
            case "IN_PROGRESS" -> "En cours";
            case "COMPLETED" -> "Terminé";
            case "CANCELLED" -> "Annulé";
            default -> status;
        };
    }

    /** Sends professionally designed bilingual (EN/FR) confirmation email when client paid FULL amount. */
    @Async
    public void sendConfirmationEmailFullPayment(String to, String clientName, String ref, java.time.LocalDate eventDate,
                                                 String eventType, int guestCount, String packagesText,
                                                 String totalRwf) {
        String subject = "Madeleine Group - Booking Confirmed & Fully Paid / Réservation confirmée et payée";
        String body = buildFullPaymentEmailHtml(clientName, ref, eventDate, eventType, guestCount, packagesText, totalRwf);
        sendHtmlEmail(to, subject, body);
    }

    /** Sends professionally designed bilingual (EN/FR) confirmation email when client paid PARTIAL amount. */
    @Async
    public void sendConfirmationEmailPartialPayment(String to, String clientName, String ref, java.time.LocalDate eventDate,
                                                    String eventType, int guestCount, String packagesText,
                                                    String totalRwf, String paidRwf, String remainingRwf) {
        String subject = "Madeleine Group - Booking Confirmed / Réservation confirmée (paiement partiel)";
        String body = buildPartialPaymentEmailHtml(clientName, ref, eventDate, eventType, guestCount, packagesText, totalRwf, paidRwf, remainingRwf);
        sendHtmlEmail(to, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) return;
        try {
            var message = mailSender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, java.nio.charset.StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    /** Sends HTML email synchronously; throws on failure so caller can return proper API response. */
    private void sendHtmlEmailSync(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) throw new IllegalArgumentException("Recipient email is required");
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, java.nio.charset.StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Payment confirmation email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a premium bilingual (FR/EN) payment confirmation email when the booking is fully paid.
     * Design: round logo, teal header, green checkmark, info card, warm message, footer.
     */
    public void sendPaymentConfirmationEmailSync(String to, String clientName, String bookingRef,
                                                 String eventType, String eventDateStr, String totalRwf) {
        String html = buildPaymentConfirmationEmailHtml(clientName, bookingRef, eventType, eventDateStr, totalRwf);
        sendHtmlEmailSync(to, "Madeleine Group — Confirmation de paiement / Payment confirmed", html);
    }

    private String buildPaymentConfirmationEmailHtml(String clientName, String bookingRef,
                                                     String eventType, String eventDateStr, String totalRwf) {
        String logoTag = buildLogoImgTag();
        String name = (clientName != null && !clientName.isBlank()) ? clientName : "Client";
        String headerTitle = "CONFIRMATION DE PAIEMENT / PAYMENT CONFIRMED";

        StringBuilder body = new StringBuilder();
        body.append("<div style=\"text-align:center;margin-bottom:28px;\">");
        body.append("<div style=\"width:80px;height:80px;border-radius:50%;background:linear-gradient(135deg,#16a34a 0%,#22c55e 100%);display:inline-flex;align-items:center;justify-content:center;margin-bottom:16px;box-shadow:0 4px 14px rgba(22,163,74,0.35);\">");
        body.append("<span style=\"font-size:42px;color:#fff;line-height:1;font-weight:bold;display:flex;align-items:center;justify-content:center;width:100%;height:100%;margin:0;padding:0;\">✓</span>");
        body.append("</div>");
        body.append("<p style=\"margin:0;font-size:20px;font-weight:700;color:#16a34a;letter-spacing:0.02em;\">Paiement reçu · Payment received</p>");
        body.append("</div>");

        body.append("<p style=\"margin:0 0 12px;font-size:16px;line-height:1.65;color:#334155;\">");
        body.append("Cher/Chère <strong>").append(escapeHtml(name)).append("</strong>,<br/>");
        body.append("Nous confirmons avec plaisir que votre paiement a été reçu en totalité pour votre événement avec Madeleine Group.");
        body.append("</p>");
        body.append("<p style=\"margin:0 0 20px;font-size:16px;line-height:1.65;color:#334155;\">");
        body.append("Dear <strong>").append(escapeHtml(name)).append("</strong>,<br/>");
        body.append("We are delighted to confirm that your payment has been received in full for your event with Madeleine Group.");
        body.append("</p>");

        body.append("<div style=\"background:#f0fdf4;border:1px solid #bbf7d0;border-radius:12px;padding:20px 24px;margin:20px 0;\">");
        body.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:14px;color:#1e293b;\">");
        body.append("<tr><td style=\"padding:8px 0;color:#64748b;width:42%;\">Référence / Reference</td><td style=\"padding:8px 0;font-weight:600;\">").append(escapeHtml(bookingRef)).append("</td></tr>");
        body.append("<tr><td style=\"padding:8px 0;color:#64748b;\">Événement / Event</td><td style=\"padding:8px 0;\">").append(escapeHtml(eventType)).append("</td></tr>");
        body.append("<tr><td style=\"padding:8px 0;color:#64748b;\">Date</td><td style=\"padding:8px 0;\">").append(escapeHtml(eventDateStr)).append("</td></tr>");
        body.append("<tr><td style=\"padding:10px 0 4px;color:#64748b;\">Montant total / Total amount</td><td style=\"padding:10px 0 4px;font-weight:700;font-size:16px;color:#16a34a;\">").append(escapeHtml(totalRwf != null ? totalRwf : "0")).append(" RWF</td></tr>");
        body.append("<tr><td style=\"padding:8px 0;color:#64748b;\">Statut / Status</td><td style=\"padding:8px 0;\">");
        body.append("<span style=\"display:inline-block;background:#16a34a;color:#fff;font-size:12px;font-weight:700;padding:6px 14px;border-radius:999px;letter-spacing:0.04em;\">PAYÉ INTÉGRALEMENT · FULLY PAID</span>");
        body.append("</td></tr>");
        body.append("</table></div>");

        body.append("<p style=\"margin:24px 0 12px;font-size:15px;line-height:1.7;color:#475569;\">");
        body.append("Toute l'équipe Madeleine Group se réjouit de rendre votre événement inoubliable. Merci pour votre confiance.");
        body.append("</p>");
        body.append("<p style=\"margin:0 0 24px;font-size:15px;line-height:1.7;color:#475569;\">");
        body.append("The entire Madeleine Group team is looking forward to making your event truly special. Thank you for trusting us with your celebration.");
        body.append("</p>");

        String phone = (contactPhone != null && !contactPhone.isBlank()) ? contactPhone.trim() : "";
        String email = (contactEmail != null && !contactEmail.isBlank()) ? contactEmail.trim() : "";
        body.append("<p style=\"margin:0;font-size:13px;color:#64748b;\">");
        if (!phone.isEmpty()) body.append(escapeHtml(phone));
        if (!phone.isEmpty() && !email.isEmpty()) body.append(" · ");
        if (!email.isEmpty()) body.append("<a href=\"mailto:").append(escapeHtml(email)).append("\" style=\"color:#0d6e6e;text-decoration:none;\">").append(escapeHtml(email)).append("</a>");
        body.append("</p>");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>Madeleine Group — Payment confirmed</title></head>");
        html.append("<body style=\"margin:0;font-family:'Segoe UI',Tahoma,Arial,sans-serif;background:#f4f7f6;\">");
        html.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f7f6;padding:32px 16px;\"><tr><td align=\"center\">");
        html.append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background:#fff;border-radius:12px;box-shadow:0 8px 24px rgba(0,0,0,0.08);overflow:hidden;\">");
        html.append("<tr><td style=\"background:linear-gradient(135deg,#0d6e6e 0%,#0f766e 100%);padding:36px 32px;text-align:center;\">");
        html.append("<div style=\"margin-bottom:14px;\">").append(logoTag).append("</div>");
        html.append("<p style=\"margin:0;font-size:18px;font-weight:700;color:#fff;letter-spacing:0.08em;\">").append(escapeHtml(headerTitle)).append("</p>");
        html.append("</td></tr>");
        html.append("<tr><td style=\"padding:40px 36px;color:#1e293b;\">").append(body.toString()).append("</td></tr>");
        html.append("<tr><td style=\"background:#f1f5f9;padding:24px;text-align:center;\">");
        html.append("<p style=\"margin:0;font-size:12px;color:#94a3b8;\">© Madeleine Group — Événements premium · Rwanda</p>");
        html.append("</td></tr>");
        html.append("</table></td></tr></table></body></html>");
        return html.toString();
    }

    private static String buildFullPaymentEmailHtml(String clientName, String ref, java.time.LocalDate eventDate,
                                                    String eventType, int guestCount, String packagesText,
                                                    String totalRwf) {
        String en = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;'>" +
                "<div style='background:linear-gradient(135deg,#1a472a 0%,#2d5a3d 100%);color:white;padding:24px;border-radius:8px 8px 0 0;'>" +
                "<h1 style='margin:0;font-size:22px;'>Madeleine Group Premium</h1>" +
                "<p style='margin:8px 0 0;opacity:0.95;'>Booking Confirmed & Fully Paid</p></div>" +
                "<div style='background:#f8f9fa;padding:24px;border:1px solid #e9ecef;border-top:none;border-radius:0 0 8px 8px;'>" +
                "<p style='font-size:16px;color:#333;'>Dear <strong>" + escapeHtml(clientName) + "</strong>,</p>" +
                "<p>We are delighted to confirm your booking with Madeleine Group. <strong style='color:#1a472a;'>Your payment has been received in full.</strong></p>" +
                "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>" +
                "<tr style='background:#e9ecef;'><td style='padding:10px;'><strong>Booking Reference</strong></td><td style='padding:10px;'>" + escapeHtml(ref) + "</td></tr>" +
                "<tr><td style='padding:10px;'><strong>Event Date</strong></td><td style='padding:10px;'>" + escapeHtml(eventDate != null ? eventDate.toString() : "") + "</td></tr>" +
                "<tr style='background:#e9ecef;'><td style='padding:10px;'><strong>Event Type</strong></td><td style='padding:10px;'>" + escapeHtml(eventType != null ? eventType : "") + "</td></tr>" +
                "<tr><td style='padding:10px;'><strong>Guests</strong></td><td style='padding:10px;'>" + guestCount + "</td></tr>" +
                "<tr style='background:#1a472a;color:white;'><td style='padding:12px;'><strong>Total Paid</strong></td><td style='padding:12px;'><strong>" + escapeHtml(totalRwf != null ? totalRwf : "0") + " RWF</strong></td></tr>" +
                "</table>" +
                "<p><strong>Selected services:</strong></p><pre style='background:white;padding:12px;border-radius:4px;font-size:14px;'>" + escapeHtml(packagesText != null ? packagesText : "None") + "</pre>" +
                "<p>Your event is officially confirmed. We look forward to welcoming you!</p>" +
                "<p style='margin-top:24px;'>Warm regards,<br><strong>The Madeleine Group Team</strong></p></div>" +
                "<hr style='border:none;border-top:2px solid #1a472a;margin:24px 0;'>" +
                "<div style='background:linear-gradient(135deg,#1a472a 0%,#2d5a3d 100%);color:white;padding:24px;border-radius:8px 8px 0 0;'>" +
                "<h2 style='margin:0;font-size:18px;'>Réservation confirmée et payée</h2></div>" +
                "<div style='background:#f8f9fa;padding:24px;border:1px solid #e9ecef;border-top:none;border-radius:0 0 8px 8px;'>" +
                "<p>Cher/Chère <strong>" + escapeHtml(clientName) + "</strong>,</p>" +
                "<p>Nous sommes ravis de confirmer votre réservation chez Madeleine Group. <strong style='color:#1a472a;'>Votre paiement a été reçu en totalité.</strong></p>" +
                "<p>Référence: " + escapeHtml(ref) + " | Date: " + escapeHtml(eventDate != null ? eventDate.toString() : "") + " | Montant payé: " + escapeHtml(totalRwf != null ? totalRwf : "0") + " RWF</p>" +
                "<p>Votre événement est officiellement confirmé. Au plaisir de vous accueillir!</p>" +
                "<p>Cordialement,<br><strong>L'équipe Madeleine Group</strong></p></div></div>";
        return en;
    }

    private static String buildPartialPaymentEmailHtml(String clientName, String ref, java.time.LocalDate eventDate,
                                                       String eventType, int guestCount, String packagesText,
                                                       String totalRwf, String paidRwf, String remainingRwf) {
        String en = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;'>" +
                "<div style='background:linear-gradient(135deg,#1a472a 0%,#2d5a3d 100%);color:white;padding:24px;border-radius:8px 8px 0 0;'>" +
                "<h1 style='margin:0;font-size:22px;'>Madeleine Group Premium</h1>" +
                "<p style='margin:8px 0 0;opacity:0.95;'>Booking Confirmed (Partial Payment)</p></div>" +
                "<div style='background:#f8f9fa;padding:24px;border:1px solid #e9ecef;border-top:none;border-radius:0 0 8px 8px;'>" +
                "<p style='font-size:16px;color:#333;'>Dear <strong>" + escapeHtml(clientName) + "</strong>,</p>" +
                "<p>We are pleased to confirm your booking with Madeleine Group.</p>" +
                "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>" +
                "<tr style='background:#e9ecef;'><td style='padding:10px;'><strong>Booking Reference</strong></td><td style='padding:10px;'>" + escapeHtml(ref) + "</td></tr>" +
                "<tr><td style='padding:10px;'><strong>Event Date</strong></td><td style='padding:10px;'>" + escapeHtml(eventDate != null ? eventDate.toString() : "") + "</td></tr>" +
                "<tr style='background:#e9ecef;'><td style='padding:10px;'><strong>Amount Paid</strong></td><td style='padding:10px;color:#1a472a;'><strong>" + escapeHtml(paidRwf != null ? paidRwf : "0") + " RWF</strong></td></tr>" +
                "<tr><td style='padding:10px;'><strong>Total Amount</strong></td><td style='padding:10px;'>" + escapeHtml(totalRwf != null ? totalRwf : "0") + " RWF</td></tr>" +
                "<tr style='background:#fff3cd;'><td style='padding:12px;'><strong>Remaining Balance</strong></td><td style='padding:12px;'><strong>" + escapeHtml(remainingRwf != null ? remainingRwf : "0") + " RWF</strong></td></tr>" +
                "</table>" +
                "<p>Please kindly settle the remaining balance of <strong>" + escapeHtml(remainingRwf != null ? remainingRwf : "0") + " RWF</strong> before your event date. We thank you for your cooperation.</p>" +
                "<p>Selected services:</p><pre style='background:white;padding:12px;border-radius:4px;font-size:14px;'>" + escapeHtml(packagesText != null ? packagesText : "None") + "</pre>" +
                "<p style='margin-top:24px;'>Warm regards,<br><strong>The Madeleine Group Team</strong></p></div>" +
                "<hr style='border:none;border-top:2px solid #1a472a;margin:24px 0;'>" +
                "<div style='background:linear-gradient(135deg,#1a472a 0%,#2d5a3d 100%);color:white;padding:24px;border-radius:8px 8px 0 0;'>" +
                "<h2 style='margin:0;font-size:18px;'>Réservation confirmée (paiement partiel)</h2></div>" +
                "<div style='background:#f8f9fa;padding:24px;border:1px solid #e9ecef;border-top:none;border-radius:0 0 8px 8px;'>" +
                "<p>Cher/Chère <strong>" + escapeHtml(clientName) + "</strong>,</p>" +
                "<p>Nous avons le plaisir de confirmer votre réservation. Montant payé: <strong>" + escapeHtml(paidRwf != null ? paidRwf : "0") + " RWF</strong>. Solde restant: <strong>" + escapeHtml(remainingRwf != null ? remainingRwf : "0") + " RWF</strong>.</p>" +
                "<p>Veuillez régler le solde restant avant la date de votre événement. Merci de votre coopération.</p>" +
                "<p>Cordialement,<br><strong>L'équipe Madeleine Group</strong></p></div></div>";
        return en;
    }

    @Async
    public void sendContactInquiryReplyEmail(String toEmail, String originalSubject, String recipientName,
                                             String originalMessage, String replyBody, String repliedByName) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }
        try {
            String subj = "Re: " + (originalSubject != null ? originalSubject : "your message") + " — Madeleine Group";
            String inner = "<p style=\"margin:0 0 12px;font-size:15px;color:#475569;\">Dear "
                    + escapeHtml(recipientName != null ? recipientName : "visitor") + ",</p>"
                    + "<p style=\"margin:0 0 18px;font-size:15px;line-height:1.65;color:#475569;\">"
                    + "Thank you for contacting <strong style=\"color:#0d6e6e;\">Madeleine Group</strong>. "
                    + "Here is our reply from <strong>" + escapeHtml(repliedByName != null ? repliedByName : "our team")
                    + "</strong>:</p>"
                    + "<div style=\"background:#ecfdf5;border-left:4px solid #0d6e6e;padding:16px;border-radius:8px;margin:0 0 20px;\">"
                    + plainToHtml(replyBody != null ? replyBody : "")
                    + "</div>"
                    + "<p style=\"margin:0 0 8px;font-size:12px;font-weight:700;color:#64748b;text-transform:uppercase;\">Your original message</p>"
                    + "<div style=\"background:#f8fafc;padding:14px;border-radius:8px;border:1px solid #e2e8f0;font-size:14px;color:#475569;\">"
                    + plainToHtml(originalMessage != null ? originalMessage : "")
                    + "</div>";
            String html = buildBrandedEmailWrapper("RÉPONSE / REPLY", inner);
            sendHtmlEmail(toEmail.trim(), subj, html);
            log.info("Contact inquiry reply email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed contact inquiry reply email: {}", e.getMessage());
        }
    }

    @Async
    public void sendNewAccountDeletionRequestAlert(java.util.List<String> adminEmails, String userName,
                                                   String userEmail, String reason) {
        if (adminEmails == null || adminEmails.isEmpty()) {
            return;
        }
        String body = "<p style=\"margin:0 0 12px;font-size:15px;color:#b45309;\"><strong>New account deletion request</strong></p>"
                + "<p style=\"margin:0 0 8px;font-size:14px;color:#475569;\">User: <strong>"
                + escapeHtml(userName) + "</strong></p>"
                + "<p style=\"margin:0 0 8px;font-size:14px;color:#475569;\">Email: "
                + escapeHtml(userEmail != null ? userEmail : "") + "</p>"
                + "<p style=\"margin:0 0 8px;font-size:14px;color:#475569;\">Reason:</p>"
                + "<div style=\"background:#fffbeb;border:1px solid #fcd34d;padding:12px;border-radius:8px;font-size:14px;\">"
                + plainToHtml(reason != null && !reason.isBlank() ? reason : "(none provided)")
                + "</div>"
                + "<p style=\"margin:16px 0 0;font-size:13px;color:#64748b;\">Review this request in the CEO dashboard → Delete Requests.</p>";
        String html = buildBrandedEmailWrapper("DEMANDE DE SUPPRESSION / DELETION REQUEST", body);
        String subject = "⚠️ New account deletion request — " + (userName != null ? userName : "user");
        for (String to : adminEmails) {
            if (to == null || to.isBlank()) continue;
            try {
                sendHtmlEmail(to.trim(), subject, html);
            } catch (Exception e) {
                log.error("Failed deletion alert to {}: {}", to, e.getMessage());
            }
        }
    }

    @Async
    public void sendAccountDeletedEmail(String toEmail, String userName, String optionalNote) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }
        try {
            String inner = "<p style=\"margin:0 0 14px;font-size:16px;color:#1e293b;\">Dear "
                    + escapeHtml(userName != null ? userName : "user") + ",</p>"
                    + "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.65;color:#475569;\">"
                    + "Your Madeleine Group account has been permanently deleted as approved.</p>"
                    + (optionalNote != null && !optionalNote.isBlank()
                    ? "<div style=\"background:#f1f5f9;padding:14px;border-radius:8px;margin:0 0 16px;font-size:14px;\">"
                    + plainToHtml(optionalNote) + "</div>"
                    : "")
                    + "<p style=\"margin:0;font-size:14px;color:#64748b;\">Thank you for having been part of our community. "
                    + "We wish you all the best.</p>"
                    + "<p style=\"margin:12px 0 0;font-size:14px;color:#475569;\">— Madeleine Group</p>";
            String html = buildBrandedEmailWrapper("COMPTE SUPPRIMÉ / ACCOUNT DELETED", inner);
            sendHtmlEmail(toEmail.trim(), "Your account has been deleted — Madeleine Group", html);
        } catch (Exception e) {
            log.error("Failed account deleted email: {}", e.getMessage());
        }
    }

    @Async
    public void sendDeleteRequestRejectedEmail(String toEmail, String userName, String reason) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }
        try {
            String inner = "<p style=\"margin:0 0 14px;font-size:16px;color:#1e293b;\">Dear "
                    + escapeHtml(userName != null ? userName : "user") + ",</p>"
                    + "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.65;color:#475569;\">"
                    + "Your request to delete your Madeleine Group account was <strong>not approved</strong> at this time.</p>"
                    + "<p style=\"margin:0 0 8px;font-size:13px;font-weight:600;color:#64748b;\">Reason from the team</p>"
                    + "<div style=\"background:#fef2f2;border-left:4px solid #ef4444;padding:14px;border-radius:8px;\">"
                    + plainToHtml(reason != null ? reason : "")
                    + "</div>"
                    + "<p style=\"margin:18px 0 0;font-size:14px;color:#475569;\">We hope you will continue to enjoy our services. "
                    + "If you have questions, please contact us.</p>";
            String html = buildBrandedEmailWrapper("DEMANDE REFUSÉE / REQUEST NOT APPROVED", inner);
            sendHtmlEmail(toEmail.trim(), "Your deletion request was not approved — Madeleine Group", html);
        } catch (Exception e) {
            log.error("Failed rejection email: {}", e.getMessage());
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Sends bilingual email when testimonial is approved and published. */
    @Async
    public void sendTestimonialApprovedEmail(String to, String clientName) {
        if (to == null || to.isBlank()) return;
        try {
            String subject = "Your testimonial is now live on our website! | Votre témoignage est maintenant en ligne sur notre site !";
            String en = "Dear " + (clientName != null ? clientName : "Valued Client") + ",\n\n" +
                    "We are delighted to let you know that your testimonial has been selected and is now featured on the Madeleine Group website!\n\n" +
                    "Your kind words mean the world to us and help future clients discover the quality of our services.\n\n" +
                    "Thank you for trusting Madeleine Group with your special moments.\n\n" +
                    "Warm regards,\nThe Madeleine Group Team\nwww.madeleinegroup.rw";
            String fr = "Cher(e) " + (clientName != null ? clientName : "Client") + ",\n\n" +
                    "Nous avons le plaisir de vous informer que votre témoignage a été sélectionné et est désormais publié sur le site web de Madeleine Group !\n\n" +
                    "Vos mots bienveillants nous touchent profondément et aident nos futurs clients à découvrir la qualité de nos services.\n\n" +
                    "Merci de faire confiance à Madeleine Group pour vos moments spéciaux.\n\n" +
                    "Cordialement,\nL'équipe Madeleine Group\nwww.madeleinegroup.rw";
            sendNotificationEmail(to, subject, en + "\n\n---\n\n" + fr);
            log.info("Testimonial approved email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send testimonial approved email to {}: {}", to, e.getMessage());
        }
    }

    /** Sends "Joyeux anniversaire" email to clients on the anniversary of their Birthday ceremony. */
    @Async
    public void sendBirthdayAnniversaryEmail(String to, String clientName, int yearsSinceEvent) {
        if (to == null || to.isBlank()) return;
        try {
            String yearsText = yearsSinceEvent == 1 ? "1 an" : yearsSinceEvent + " ans";
            String en = "Dear " + (clientName != null ? clientName : "Valued Client") + ",\n\n" +
                    "Happy anniversary! It has been " + yearsText + " since we celebrated your special Birthday event with you at Madeleine Group.\n\n" +
                    "We hope this past year has been wonderful, and we wish you all the best for the year ahead.\n\n" +
                    "Warm regards,\nThe Madeleine Group Team";

            String fr = "Cher/Chère " + (clientName != null ? clientName : "Client") + ",\n\n" +
                    "Joyeux anniversaire ! Cela fait " + yearsText + " que nous avons célébré votre événement d'anniversaire avec vous chez Madeleine Group.\n\n" +
                    "Nous espérons que cette année passée a été merveilleuse et vous souhaitons le meilleur pour l'année à venir.\n\n" +
                    "Cordialement,\nL'équipe Madeleine Group";

            sendNotificationEmail(to, "Madeleine Group - Joyeux anniversaire / Happy Anniversary", en + "\n\n---\n\n" + fr);
            log.info("Birthday anniversary email sent to {} ({} years)", to, yearsSinceEvent);
        } catch (Exception e) {
            log.error("Failed to send birthday anniversary email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Client acknowledgement after a payment is recorded (finance income or debt installment).
     */
    public void sendPaymentReceivedAcknowledgmentSync(String toEmail, String clientName, String bookingRef,
                                                      java.math.BigDecimal amount, String paymentMethodLabel,
                                                      java.time.LocalDate paymentDate,
                                                      java.math.BigDecimal remainingOnBooking, boolean fullyPaid) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }
        String name = clientName != null ? clientName : "Client";
        String amt = amount != null ? amount.stripTrailingZeros().toPlainString() : "0";
        String rem = remainingOnBooking != null ? remainingOnBooking.stripTrailingZeros().toPlainString() : "0";
        String meth = paymentMethodLabel != null && !paymentMethodLabel.isBlank() ? paymentMethodLabel : "—";
        String pdate = paymentDate != null ? paymentDate.toString() : "—";
        String statusLine = fullyPaid
                ? "<p style=\"margin:16px 0 0;font-size:15px;font-weight:600;color:#15803d;\">✓ Your booking balance is fully settled.</p>"
                : "<p style=\"margin:16px 0 0;font-size:15px;color:#475569;\">Remaining on this booking: <strong>"
                + escapeHtml(rem) + " RWF</strong></p>";

        String inner = "<p style=\"margin:0 0 16px;font-size:16px;font-weight:600;color:#1e293b;\">Dear "
                + escapeHtml(name) + ",</p>"
                + "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.65;color:#475569;\">Thank you — we have received your payment.</p>"
                + "<div style=\"background:#f8fafc;border-radius:8px;padding:18px;border:1px solid #e2e8f0;\">"
                + "<table role=\"presentation\" style=\"width:100%;font-size:14px;color:#334155;\">"
                + "<tr><td style=\"padding:6px 0;color:#64748b;width:42%;\">Booking</td><td style=\"padding:6px 0;font-weight:600;\">"
                + escapeHtml(bookingRef != null ? bookingRef : "—") + "</td></tr>"
                + "<tr><td style=\"padding:6px 0;color:#64748b;\">Amount received</td><td style=\"padding:6px 0;font-weight:600;color:#0d6e6e;\">"
                + escapeHtml(amt) + " RWF</td></tr>"
                + "<tr><td style=\"padding:6px 0;color:#64748b;\">Date</td><td style=\"padding:6px 0;\">" + escapeHtml(pdate) + "</td></tr>"
                + "<tr><td style=\"padding:6px 0;color:#64748b;\">Method</td><td style=\"padding:6px 0;\">" + escapeHtml(meth) + "</td></tr>"
                + "</table></div>"
                + statusLine
                + "<p style=\"margin:20px 0 0;font-size:14px;color:#64748b;\">Questions? Reply to this email.</p>"
                + "<p style=\"margin:8px 0 0;font-size:14px;color:#475569;\">Warm regards,<br><strong style=\"color:#0d6e6e;\">Madeleine Group</strong></p>";

        String html = buildBrandedEmailWrapper("CONFIRMATION DE PAIEMENT / PAYMENT RECEIVED", inner);
        sendHtmlEmailSync(toEmail.trim(), "Madeleine Group — Payment received / Paiement reçu", html);
    }

    /** Formal invoice / receipt email for a fully paid booking (HTML). */
    public void sendInvoiceEmailSync(rw.madeleinegroup.entity.Booking booking) {
        if (booking.getClient() == null) {
            throw new IllegalArgumentException("Booking has no client");
        }
        String to = booking.getClient().getEmail();
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Client has no email address");
        }
        String clientName = booking.getClient().getFullName() != null ? booking.getClient().getFullName() : "Client";
        String ref = booking.getBookingReference() != null ? booking.getBookingReference() : "—";
        String branch = booking.getBranch() != null && booking.getBranch().getName() != null
                ? booking.getBranch().getName() : "—";
        String ev = booking.getEventDate() != null ? booking.getEventDate().toString() : "—";
        String paid = booking.getPaidAmount() != null ? booking.getPaidAmount().stripTrailingZeros().toPlainString()
                : (booking.getEstimatedAmount() != null ? booking.getEstimatedAmount().stripTrailingZeros().toPlainString() : "0");

        StringBuilder rows = new StringBuilder();
        if (booking.getBookingPackages() != null) {
            for (rw.madeleinegroup.entity.BookingPackage bp : booking.getBookingPackages()) {
                String pkg = bp.getPackageItem() != null && bp.getPackageItem().getName() != null
                        ? bp.getPackageItem().getName() : "Item";
                String line = bp.getTotalPrice() != null ? bp.getTotalPrice().stripTrailingZeros().toPlainString()
                        : (bp.getUnitPrice() != null ? bp.getUnitPrice().stripTrailingZeros().toPlainString() : "0");
                rows.append("<tr><td style=\"padding:10px 8px;border-bottom:1px solid #e2e8f0;\">")
                        .append(escapeHtml(pkg)).append("</td><td style=\"padding:10px 8px;border-bottom:1px solid #e2e8f0;text-align:right;font-weight:600;\">")
                        .append(escapeHtml(line)).append(" RWF</td></tr>");
            }
        }
        if (rows.length() == 0) {
            rows.append("<tr><td colspan=\"2\" style=\"padding:10px 8px;color:#64748b;\">Package details on file</td></tr>");
        }

        String inner = "<p style=\"margin:0 0 8px;font-size:16px;font-weight:600;color:#1e293b;\">Invoice / Facture</p>"
                + "<p style=\"margin:0 0 20px;font-size:14px;color:#64748b;\">Madeleine Group — " + escapeHtml(branch) + "</p>"
                + "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:#475569;\">Dear <strong>" + escapeHtml(clientName) + "</strong>,</p>"
                + "<p style=\"margin:0 0 16px;font-size:15px;color:#475569;\">Below is your receipt for paid services.</p>"
                + "<div style=\"background:#f8fafc;border-radius:8px;padding:16px;margin-bottom:16px;border:1px solid #e2e8f0;\">"
                + "<table role=\"presentation\" style=\"width:100%;font-size:14px;\">"
                + "<tr><td style=\"padding:6px 0;color:#64748b;\">Reference</td><td style=\"padding:6px 0;font-weight:700;\">" + escapeHtml(ref) + "</td></tr>"
                + "<tr><td style=\"padding:6px 0;color:#64748b;\">Event date</td><td style=\"padding:6px 0;\">" + escapeHtml(ev) + "</td></tr>"
                + "<tr><td style=\"padding:6px 0;color:#64748b;\">Total paid</td><td style=\"padding:6px 0;font-weight:700;color:#0d6e6e;\">"
                + escapeHtml(paid) + " RWF</td></tr>"
                + "</table></div>"
                + "<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;font-size:14px;margin-bottom:16px;\">"
                + "<tr style=\"background:#e8f4f4;\"><th style=\"text-align:left;padding:10px 8px;\">Item</th>"
                + "<th style=\"text-align:right;padding:10px 8px;\">Amount</th></tr>"
                + rows
                + "</table>"
                + "<p style=\"margin:0;font-size:13px;color:#64748b;\">This email serves as proof of payment. Thank you for choosing Madeleine Group.</p>";

        String html = buildBrandedEmailWrapper("FACTURE / INVOICE", inner);
        sendHtmlEmailSync(to.trim(), "Madeleine Group — Invoice / Facture " + ref, html);
    }
}
