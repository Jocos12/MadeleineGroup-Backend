package rw.madeleinegroup.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.Locale;

/**
 * Who can see an announcement in the dashboard. Stored as VARCHAR in MySQL (see Flyway migration).
 */
public enum AnnouncementAudience {
    /** CEO, MANAGER, ADMIN */
    ALL_TEAM,
    CEO,
    /** Legacy rows */
    CEO_ONLY,
    MANAGER,
    ADMIN,
    CLIENT,
    OTHERS,
    /** All roles */
    EVERYONE;

    @JsonCreator
    public static AnnouncementAudience fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("audience cannot be null or blank");
        }
        try {
            return AnnouncementAudience.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid audience: '" + value + "'. Allowed values: "
                            + Arrays.toString(AnnouncementAudience.values()),
                    e
            );
        }
    }
}
