package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.ContactInquiry;

import java.time.LocalDateTime;

public record ContactInquiryDto(
        Long id,
        String name,
        String email,
        String subject,
        String message,
        boolean read,
        boolean replied,
        String replyMessage,
        LocalDateTime createdAt,
        LocalDateTime repliedAt
) {
    public static ContactInquiryDto from(ContactInquiry c) {
        return new ContactInquiryDto(
                c.getId(),
                c.getName(),
                c.getEmail(),
                c.getSubject(),
                c.getMessage(),
                c.isRead(),
                c.isReplied(),
                c.getReplyMessage(),
                c.getCreatedAt(),
                c.getRepliedAt()
        );
    }
}
