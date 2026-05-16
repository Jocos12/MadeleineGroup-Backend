package rw.madeleinegroup.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import rw.madeleinegroup.entity.Booking;
import rw.madeleinegroup.entity.BookingStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class BookingSpecification {

    private BookingSpecification() {}

    /**
     * @param overdueOnly if true, only bookings whose event date is strictly before today and status is CONFIRMED or IN_PROGRESS (same as analytics "overdue").
     */
    public static Specification<Booking> searchBookings(String query, BookingStatus status,
                                                        Long branchId, String eventType,
                                                        LocalDate dateFrom, LocalDate dateTo,
                                                        Boolean overdueOnly) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Boolean.TRUE.equals(overdueOnly)) {
                LocalDate today = LocalDate.now();
                predicates.add(cb.lessThan(root.get("eventDate"), today));
                predicates.add(root.get("status").in(BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS));
            }

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.toLowerCase().trim() + "%";
                var client = root.join("client");
                Predicate refMatch = cb.like(cb.lower(root.get("bookingReference")), pattern);
                Predicate clientNameMatch = cb.like(cb.lower(client.get("fullName")), pattern);
                Predicate clientEmailMatch = cb.like(cb.lower(client.get("email")), pattern);
                Predicate clientPhoneMatch = cb.like(cb.lower(cb.coalesce(client.get("phone"), "")), pattern);
                predicates.add(cb.or(refMatch, clientNameMatch, clientEmailMatch, clientPhoneMatch));
            }
            if (!Boolean.TRUE.equals(overdueOnly) && status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (branchId != null) {
                predicates.add(cb.equal(root.get("branch").get("id"), branchId));
            }
            if (eventType != null && !eventType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("eventType")), eventType.toLowerCase().trim()));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), dateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
