package rw.madeleinegroup.dto;

import rw.madeleinegroup.entity.LoginAudit;

import java.time.format.DateTimeFormatter;

public record RecentLoginDto(
        Long id,
        String email,
        String fullName,
        String role,
        String loggedAt,
        String ipAddress,
        String userAgent
) {
    public static RecentLoginDto from(LoginAudit a) {
        return new RecentLoginDto(
                a.getId(),
                a.getEmail(),
                a.getFullName(),
                a.getRole(),
                a.getLoggedAt() != null ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(a.getLoggedAt()) : null,
                a.getIpAddress(),
                a.getUserAgent()
        );
    }
}
