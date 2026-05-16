package rw.madeleinegroup.dto;

/**
 * PDF bytes and suggested filename for Content-Disposition (invoice download).
 */
public record InvoicePdfPayload(byte[] bytes, String filename) {
}
