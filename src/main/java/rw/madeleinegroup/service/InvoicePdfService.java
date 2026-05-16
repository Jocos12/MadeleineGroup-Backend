package rw.madeleinegroup.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.dto.InvoicePaymentLineDto;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.BookingPackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Branded invoice PDF (Madeleine logo, teal + gold accents, clean layout). Apache PDFBox.
 */
@Service
public class InvoicePdfService {

    private static final float MARGIN = 44f;
    private static final float BOTTOM = 52f;
    private static final float LINE_GAP = 2.5f;

    private static final float TEAL_R = 0f / 255f;
    private static final float TEAL_G = 90f / 255f;
    private static final float TEAL_B = 77f / 255f;

    private static final float GOLD_R = 255f / 255f;
    private static final float GOLD_G = 204f / 255f;
    private static final float GOLD_B = 0f;

    private static String pdfSafe(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace('\u2014', '-').replace('\u2013', '-');
        StringBuilder b = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            b.append(c >= 32 && c <= 255 ? c : '?');
        }
        return b.toString();
    }

    /** Readable amount with spaces (e.g. 700 000) */
    private static String formatAmountRw(BigDecimal v) {
        if (v == null) {
            return "0";
        }
        String plain = v.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
        boolean neg = plain.startsWith("-");
        String num = neg ? plain.substring(1) : plain;
        StringBuilder sb = new StringBuilder();
        int len = num.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append(' ');
            }
            sb.append(num.charAt(i));
        }
        return neg ? "-" + sb : sb.toString();
    }

    public byte[] buildPdf(Booking booking, List<InvoicePaymentLineDto> paymentLines, String lang) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean french = isFrench(lang);
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            float pageW = page.getMediaBox().getWidth();
            float pageH = page.getMediaBox().getHeight();

            PDImageXObject logo = loadLogo(doc);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float bannerH = 56f;
                cs.setNonStrokingColor(TEAL_R, TEAL_G, TEAL_B);
                cs.addRect(0, pageH - bannerH, pageW, bannerH);
                cs.fill();

                float logoH = 38f;
                float logoX = MARGIN;
                float logoY = pageH - bannerH / 2f - logoH / 2f;
                if (logo != null) {
                    float iw = logo.getWidth();
                    float ih = logo.getHeight();
                    float scale = logoH / ih;
                    float logoW = iw * scale;
                    float badgeSize = Math.max(logoW, logoH) + 10f;
                    float badgeCx = logoX + badgeSize / 2f;
                    float badgeCy = logoY + logoH / 2f;
                    float badgeR = badgeSize / 2f;
                    float imageSize = badgeSize - 6f;
                    float imageX = badgeCx - imageSize / 2f;
                    float imageY = badgeCy - imageSize / 2f;
                    float imageR = imageSize / 2f;
                    cs.setNonStrokingColor(1f, 1f, 1f);
                    addCirclePath(cs, badgeCx, badgeCy, badgeR);
                    cs.fill();
                    cs.saveGraphicsState();
                    addCirclePath(cs, badgeCx, badgeCy, imageR);
                    cs.clip();
                    cs.drawImage(logo, imageX, imageY, imageSize, imageSize);
                    cs.restoreGraphicsState();
                    cs.setStrokingColor(255f / 255f, 255f / 255f, 255f / 255f);
                    cs.setLineWidth(1.6f);
                    addCirclePath(cs, badgeCx, badgeCy, badgeR);
                    cs.stroke();
                    cs.setStrokingColor(GOLD_R, GOLD_G, GOLD_B);
                    cs.setLineWidth(0.9f);
                    addCirclePath(cs, badgeCx, badgeCy, badgeR + 1.7f);
                    cs.stroke();
                    logoX = logoX + badgeSize + 14f;
                }

                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                cs.newLineAtOffset(logoX, pageH - 22f);
                cs.showText("Madeleine Group");
                cs.endText();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 9f);
                cs.newLineAtOffset(logoX, pageH - 36f);
                cs.showText("Premium Events");
                cs.endText();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 15f);
                String invoiceTopTitle = french ? "FACTURE / INVOICE" : "INVOICE / FACTURE";
                float invW = PDType1Font.HELVETICA_BOLD.getStringWidth(invoiceTopTitle) / 1000f * 15f;
                cs.newLineAtOffset(pageW - MARGIN - invW, pageH - 30f);
                cs.showText(invoiceTopTitle);
                cs.endText();

                cs.setNonStrokingColor(GOLD_R, GOLD_G, GOLD_B);
                cs.addRect(0, pageH - bannerH - 3f, pageW, 3f);
                cs.fill();

                float y = pageH - bannerH - 28f;

                String branch = booking.getBranch() != null && booking.getBranch().getName() != null
                        ? booking.getBranch().getName() : "-";
                y = drawMutedCentered(cs, pdfSafe(branch), pageW, y);
                y -= 14f;

                y = drawText(cs, french ? "Facture / Invoice" : "Invoice / Facture", PDType1Font.HELVETICA_BOLD, 14f,
                        30f / 255f, 41f / 255f, 59f / 255f, MARGIN, y);
                y -= 8f;

                String clientName = booking.getClient() != null && booking.getClient().getFullName() != null
                        ? booking.getClient().getFullName() : "Client";
                String line1 = french
                        ? "Cher(e) " + pdfSafe(clientName) + ", voici votre reçu pour les services payés."
                        : "Dear " + pdfSafe(clientName) + ", below is your receipt for paid services.";
                y = drawWrapped(cs, line1, PDType1Font.HELVETICA, 10.5f,
                        51f / 255f, 65f / 255f, 85f / 255f, MARGIN, pageW - MARGIN, y);
                y -= 12f;

                String ref = booking.getBookingReference() != null ? booking.getBookingReference() : "-";
                String ev = booking.getEventDate() != null ? booking.getEventDate().toString() : "-";
                BigDecimal paidBd = booking.getPaidAmount() != null ? booking.getPaidAmount()
                        : (booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO);
                String paidStr = formatAmountRw(paidBd) + " RWF";

                float cardPad = 12f;
                float cardTop = y;
                float cardBottom = y - 58f;
                cs.setNonStrokingColor(248f / 255f, 250f / 255f, 252f / 255f);
                cs.addRect(MARGIN, cardBottom, pageW - 2 * MARGIN, cardTop - cardBottom);
                cs.fill();
                cs.setStrokingColor(0.86f, 0.89f, 0.92f);
                cs.setLineWidth(1f);
                cs.addRect(MARGIN, cardBottom, pageW - 2 * MARGIN, cardTop - cardBottom);
                cs.stroke();

                float cy = cardTop - 14f;
                cy = drawMetaLine(cs, french ? "Reference" : "Reference", pdfSafe(ref), true, MARGIN + cardPad, pageW - MARGIN - cardPad, cy);
                cy = drawMetaLine(cs, french ? "Date de l'événement" : "Event date", pdfSafe(ev), false, MARGIN + cardPad, pageW - MARGIN - cardPad, cy);
                drawMetaLine(cs, french ? "Total payé" : "Total paid", paidStr, true, MARGIN + cardPad, pageW - MARGIN - cardPad, cy);
                y = cardBottom - 16f;

                y = drawTableHeader(cs, MARGIN, pageW, y, french);

                boolean any = false;
                int rowIdx = 0;
                if (booking.getBookingPackages() != null) {
                    for (BookingPackage bp : booking.getBookingPackages()) {
                        any = true;
                        String pkg = bp.getPackageItem() != null && bp.getPackageItem().getName() != null
                                ? bp.getPackageItem().getName() : "Item";
                        String amt = formatAmountRw(bp.getTotalPrice() != null ? bp.getTotalPrice()
                                : (bp.getUnitPrice() != null ? bp.getUnitPrice() : BigDecimal.ZERO)) + " RWF";
                        y = drawTableRow(cs, pdfSafe(pkg), pdfSafe(amt), MARGIN, pageW, y, rowIdx++ % 2 == 0);
                        if (y < BOTTOM) {
                            break;
                        }
                    }
                }
                if (!any) {
                    y = drawText(cs, french ? "Détails des prestations enregistrés" : "Package details on file", PDType1Font.HELVETICA_OBLIQUE, 10f,
                            100f / 255f, 116f / 255f, 139f / 255f, MARGIN + 6f, y);
                }

                y = drawPaymentDetailsBlock(cs, paymentLines != null ? paymentLines : List.of(), MARGIN, pageW, y, BOTTOM, french);

                y -= 18f;
                cs.setStrokingColor(TEAL_R, TEAL_G, TEAL_B);
                cs.setLineWidth(0.5f);
                cs.moveTo(MARGIN, y + 8f);
                cs.lineTo(pageW - MARGIN, y + 8f);
                cs.stroke();
                y -= 6f;
                drawWrapped(cs,
                        french
                                ? "Ce document sert de preuve de paiement. Merci d'avoir choisi Madeleine Group."
                                : "This document serves as proof of payment. Thank you for choosing Madeleine Group.",
                        PDType1Font.HELVETICA, 9f,
                        100f / 255f, 116f / 255f, 139f / 255f, MARGIN, pageW - MARGIN, y);
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build invoice PDF: " + e.getMessage(), e);
        }
    }

    private static boolean isFrench(String lang) {
        if (lang == null) return false;
        String normalized = lang.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("fr") || normalized.startsWith("fr-");
    }

    private PDImageXObject loadLogo(PDDocument doc) {
        try (InputStream in = getClass().getResourceAsStream("/branding/madeleine-logo.jpeg")) {
            if (in == null) {
                return null;
            }
            byte[] bytes = in.readAllBytes();
            if (bytes.length == 0) {
                return null;
            }
            return JPEGFactory.createFromByteArray(doc, bytes);
        } catch (IOException e) {
            return null;
        }
    }

    private static float drawMutedCentered(PDPageContentStream cs, String branch, float pageW, float y) throws IOException {
        String t = "Madeleine Group — " + branch;
        float size = 9.5f;
        float tw = PDType1Font.HELVETICA.getStringWidth(pdfSafe(t)) / 1000f * size;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, size);
        cs.setNonStrokingColor(100f / 255f, 116f / 255f, 139f / 255f);
        cs.newLineAtOffset((pageW - tw) / 2f, y);
        cs.showText(pdfSafe(t));
        cs.endText();
        return y;
    }

    private static float drawMetaLine(PDPageContentStream cs, String label, String value, boolean boldVal,
                                      float xLeft, float xRight, float y) throws IOException {
        float size = 10f;
        float labelW = (xRight - xLeft) * 0.32f;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, size);
        cs.setNonStrokingColor(100f / 255f, 116f / 255f, 139f / 255f);
        cs.newLineAtOffset(xLeft, y);
        cs.showText(pdfSafe(label));
        cs.endText();
        cs.beginText();
        cs.setFont(boldVal ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, size);
        if ("Total paid".equals(label) || "Total payé".equals(label)) {
            cs.setNonStrokingColor(13f / 255f, 110f / 255f, 110f / 255f);
        } else {
            cs.setNonStrokingColor(30f / 255f, 41f / 255f, 59f / 255f);
        }
        cs.newLineAtOffset(xLeft + labelW, y);
        cs.showText(pdfSafe(value));
        cs.endText();
        return y - size - 7f;
    }

    private static float drawText(PDPageContentStream cs, String text, PDType1Font font, float size,
                                  float r, float g, float b, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(r, g, b);
        cs.newLineAtOffset(x, y);
        cs.showText(pdfSafe(text));
        cs.endText();
        return y - size - LINE_GAP;
    }

    private static float drawWrapped(PDPageContentStream cs, String text, PDType1Font font, float size,
                                     float r, float g, float b, float xLeft, float xRight, float yTop) throws IOException {
        float maxW = xRight - xLeft;
        List<String> lines = wrap(text, font, size, maxW);
        float y = yTop;
        for (String line : lines) {
            cs.beginText();
            cs.setFont(font, size);
            cs.setNonStrokingColor(r, g, b);
            cs.newLineAtOffset(xLeft, y);
            cs.showText(pdfSafe(line));
            cs.endText();
            y -= size + LINE_GAP;
        }
        return y;
    }

    private static List<String> wrap(String text, PDType1Font font, float size, float maxW) throws IOException {
        String[] words = pdfSafe(text).split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String trial = cur.isEmpty() ? w : cur + " " + w;
            float wth = font.getStringWidth(trial) / 1000f * size;
            if (wth > maxW && !cur.isEmpty()) {
                lines.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                cur = new StringBuilder(trial);
            }
        }
        if (!cur.isEmpty()) {
            lines.add(cur.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private static float drawTableHeader(PDPageContentStream cs, float xLeft, float pageW, float y, boolean french) throws IOException {
        float h = 24f;
        float yBottom = y - h;
        cs.setNonStrokingColor(232f / 255f, 244f / 255f, 244f / 255f);
        cs.addRect(xLeft, yBottom, pageW - 2 * xLeft, h);
        cs.fill();
        cs.setStrokingColor(13f / 255f, 110f / 255f, 110f / 255f);
        cs.setLineWidth(0.4f);
        cs.addRect(xLeft, yBottom, pageW - 2 * xLeft, h);
        cs.stroke();
        float mid = xLeft + (pageW - 2 * xLeft) * 0.62f;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
        cs.setNonStrokingColor(30f / 255f, 41f / 255f, 59f / 255f);
        cs.newLineAtOffset(xLeft + 8f, yBottom + 8f);
        cs.showText(french ? "Prestation" : "Item");
        cs.endText();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
        cs.setNonStrokingColor(30f / 255f, 41f / 255f, 59f / 255f);
        cs.newLineAtOffset(mid + 8f, yBottom + 8f);
        cs.showText(french ? "Montant" : "Amount");
        cs.endText();
        return yBottom - 2f;
    }

    private static float drawTableRow(PDPageContentStream cs, String item, String amount,
                                      float xLeft, float pageW, float y, boolean alt) throws IOException {
        float size = 10f;
        float rowH = 28f;
        float yBottom = y - rowH;
        if (alt) {
            cs.setNonStrokingColor(250f / 255f, 251f / 255f, 252f / 255f);
            cs.addRect(xLeft, yBottom, pageW - 2 * xLeft, rowH);
            cs.fill();
        }
        cs.setStrokingColor(220f / 255f, 227f / 255f, 234f / 255f);
        cs.setLineWidth(0.35f);
        cs.moveTo(xLeft, yBottom);
        cs.lineTo(pageW - xLeft, yBottom);
        cs.stroke();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, size);
        cs.setNonStrokingColor(30f / 255f, 41f / 255f, 59f / 255f);
        cs.newLineAtOffset(xLeft + 8f, yBottom + 9f);
        cs.showText(item);
        cs.endText();
        float aw = PDType1Font.HELVETICA_BOLD.getStringWidth(amount) / 1000f * size;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, size);
        cs.setNonStrokingColor(13f / 255f, 110f / 255f, 110f / 255f);
        cs.newLineAtOffset(pageW - xLeft - 8f - aw, yBottom + 9f);
        cs.showText(amount);
        cs.endText();
        return yBottom;
    }

    private static String shortPaymentDate(String iso) {
        if (iso == null || iso.isEmpty()) {
            return "-";
        }
        return iso.length() >= 10 ? iso.substring(0, 10) : iso;
    }

    private static String truncPdfLine(String s, int maxChars) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if (t.length() <= maxChars) {
            return t;
        }
        return t.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    private static float drawPaymentDetailsBlock(PDPageContentStream cs, List<InvoicePaymentLineDto> lines,
                                                 float xLeft, float pageW, float y, float bottomMin, boolean french) throws IOException {
        y -= 10f;
        y = drawText(cs, french ? "Détail des paiements / Payment details" : "Payment details / Détail des paiements", PDType1Font.HELVETICA_BOLD, 11f,
                13f / 255f, 110f / 255f, 110f / 255f, xLeft, y);
        y -= 14f;

        if (lines == null || lines.isEmpty()) {
            return drawText(cs,
                    french
                            ? "Aucune ligne de paiement enregistrée / No individual payment lines on file."
                            : "No individual payment lines on file / Aucune ligne de paiement enregistrée.",
                    PDType1Font.HELVETICA_OBLIQUE, 9f,
                    100f / 255f, 116f / 255f, 139f / 255f, xLeft, y);
        }

        y = drawPaymentLinesTableHeader(cs, xLeft, pageW, y, french);
        final int maxRows = 14;
        boolean alt = false;
        int shown = 0;
        for (InvoicePaymentLineDto line : lines) {
            if (y < bottomMin + 40f) {
                break;
            }
            if (shown >= maxRows) {
                break;
            }
            y = drawPaymentLineDataRow(cs, line, xLeft, pageW, y, alt);
            alt = !alt;
            shown++;
        }
        if (shown < lines.size()) {
            int omitted = lines.size() - shown;
            y -= 4f;
            y = drawText(cs, french
                            ? "+ " + omitted + " paiement(s) en plus - voir les enregistrements finance."
                            : "+ " + omitted + " more payment(s) / paiement(s) de plus - see finance records.",
                    PDType1Font.HELVETICA_OBLIQUE, 8.5f,
                    100f / 255f, 116f / 255f, 139f / 255f, xLeft, y);
        }
        return y - 6f;
    }

    private static float drawPaymentLinesTableHeader(PDPageContentStream cs, float xLeft, float pageW, float y, boolean french) throws IOException {
        float h = 22f;
        float yBottom = y - h;
        float innerW = pageW - 2 * xLeft;
        cs.setNonStrokingColor(232f / 255f, 244f / 255f, 244f / 255f);
        cs.addRect(xLeft, yBottom, innerW, h);
        cs.fill();
        cs.setStrokingColor(13f / 255f, 110f / 255f, 110f / 255f);
        cs.setLineWidth(0.4f);
        cs.addRect(xLeft, yBottom, innerW, h);
        cs.stroke();
        float dW = innerW * 0.20f;
        float mW = innerW * 0.34f;
        float xD = xLeft + 6f;
        float xM = xD + dW;
        float xS = xM + mW;
        float sz = 9f;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, sz);
        cs.setNonStrokingColor(30f / 255f, 41f / 255f, 59f / 255f);
        cs.newLineAtOffset(xD, yBottom + 7f);
        cs.showText(french ? "Date" : "Date");
        cs.endText();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, sz);
        cs.newLineAtOffset(xM, yBottom + 7f);
        cs.showText(french ? "Méthode / Mode" : "Method / Mode");
        cs.endText();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, sz);
        cs.newLineAtOffset(xS, yBottom + 7f);
        cs.showText("Source");
        cs.endText();
        String amtLabel = french ? "Montant" : "Amount";
        float aw = PDType1Font.HELVETICA_BOLD.getStringWidth(amtLabel) / 1000f * sz;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, sz);
        cs.newLineAtOffset(pageW - xLeft - 6f - aw, yBottom + 7f);
        cs.showText(amtLabel);
        cs.endText();
        return yBottom - 2f;
    }

    private static float drawPaymentLineDataRow(PDPageContentStream cs, InvoicePaymentLineDto line,
                                                float xLeft, float pageW, float y, boolean alt) throws IOException {
        String desc = line.getDescription();
        boolean hasDesc = desc != null && !desc.trim().isEmpty();
        float rowH = hasDesc ? 30f : 22f;
        float yBottom = y - rowH;
        float innerW = pageW - 2 * xLeft;
        if (alt) {
            cs.setNonStrokingColor(250f / 255f, 251f / 255f, 252f / 255f);
            cs.addRect(xLeft, yBottom, innerW, rowH);
            cs.fill();
        }
        cs.setStrokingColor(220f / 255f, 227f / 255f, 234f / 255f);
        cs.setLineWidth(0.35f);
        cs.moveTo(xLeft, yBottom);
        cs.lineTo(pageW - xLeft, yBottom);
        cs.stroke();

        float dW = innerW * 0.20f;
        float mW = innerW * 0.34f;
        float xD = xLeft + 6f;
        float xM = xD + dW;
        float xS = xM + mW;

        String dateStr = shortPaymentDate(line.getRecordedAt());
        String method = truncPdfLine(line.getMethodLabel(), 44);
        String source = truncPdfLine(line.getSource(), 16);
        BigDecimal amtBd = line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO;
        String amt = formatAmountRw(amtBd) + " RWF";

        float size = 9f;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, size);
        cs.setNonStrokingColor(30f / 255f, 41f / 255f, 59f / 255f);
        cs.newLineAtOffset(xD, yBottom + (hasDesc ? 14f : 7f));
        cs.showText(pdfSafe(dateStr));
        cs.endText();
        cs.beginText();
        cs.newLineAtOffset(xM, yBottom + (hasDesc ? 14f : 7f));
        cs.showText(pdfSafe(method));
        cs.endText();
        cs.beginText();
        cs.newLineAtOffset(xS, yBottom + (hasDesc ? 14f : 7f));
        cs.showText(pdfSafe(source));
        cs.endText();
        float aw = PDType1Font.HELVETICA_BOLD.getStringWidth(pdfSafe(amt)) / 1000f * size;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, size);
        cs.setNonStrokingColor(13f / 255f, 110f / 255f, 110f / 255f);
        cs.newLineAtOffset(pageW - xLeft - 6f - aw, yBottom + (hasDesc ? 14f : 7f));
        cs.showText(pdfSafe(amt));
        cs.endText();

        if (hasDesc) {
            String one = truncPdfLine(desc.trim(), 118);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 8f);
            cs.setNonStrokingColor(100f / 255f, 116f / 255f, 139f / 255f);
            cs.newLineAtOffset(xLeft + 8f, yBottom + 5f);
            cs.showText(pdfSafe(one));
            cs.endText();
        }
        return yBottom;
    }

    private static void addCirclePath(PDPageContentStream cs, float cx, float cy, float r) throws IOException {
        float c = 0.552284749831f * r;
        cs.moveTo(cx + r, cy);
        cs.curveTo(cx + r, cy + c, cx + c, cy + r, cx, cy + r);
        cs.curveTo(cx - c, cy + r, cx - r, cy + c, cx - r, cy);
        cs.curveTo(cx - r, cy - c, cx - c, cy - r, cx, cy - r);
        cs.curveTo(cx + c, cy - r, cx + r, cy - c, cx + r, cy);
        cs.closePath();
    }
}
