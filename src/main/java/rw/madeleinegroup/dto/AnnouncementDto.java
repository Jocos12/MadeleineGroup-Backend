package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.Announcement;

import java.time.LocalDateTime;
import java.util.List;

public record AnnouncementDto(
        Long id,
        String title,
        String body,
        String audience,
        boolean active,
        String createdByName,
        LocalDateTime createdAt,
        List<String> imageUrls,
        int emailNotifiedCount
) {
    public static AnnouncementDto from(Announcement a) {
        String name = null;
        if (a.getCreatedBy() != null) {
            name = a.getCreatedBy().getFullName() != null && !a.getCreatedBy().getFullName().isBlank()
                    ? a.getCreatedBy().getFullName()
                    : a.getCreatedBy().getEmail();
        }
        List<String> urls = a.getImageUrls() == null || a.getImageUrls().isEmpty()
                ? List.of()
                : List.copyOf(a.getImageUrls());
        return new AnnouncementDto(
                a.getId(),
                a.getTitle(),
                a.getBody(),
                a.getAudience().name(),
                a.isActive(),
                name,
                a.getCreatedAt(),
                urls,
                a.getEmailNotifiedCount()
        );
    }
}
