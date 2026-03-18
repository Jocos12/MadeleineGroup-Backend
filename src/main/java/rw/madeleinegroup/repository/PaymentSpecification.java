package rw.madeleinegroup.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PaymentSpecification {

    private PaymentSpecification() {}

    public static Specification<Payment> searchPayments(String query, String typeStr, Long branchId,
                                                        PaymentMethod paymentMethod, String paymentStatusStr,
                                                        LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.toLowerCase().trim() + "%";
                var client = root.join("client", jakarta.persistence.criteria.JoinType.LEFT);
                Predicate clientName = cb.like(cb.lower(cb.coalesce(client.get("fullName"), "")), pattern);
                Predicate clientEmail = cb.like(cb.lower(cb.coalesce(client.get("email"), "")), pattern);
                Predicate descMatch = cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern);
                var booking = root.join("booking", jakarta.persistence.criteria.JoinType.LEFT);
                Predicate refMatch = cb.like(cb.lower(cb.coalesce(booking.get("bookingReference"), "")), pattern);
                predicates.add(cb.or(clientName, clientEmail, descMatch, refMatch));
            }
            if (typeStr != null && !typeStr.isBlank()) {
                predicates.add(cb.equal(root.get("type"), Payment.PaymentType.valueOf(typeStr.toUpperCase())));
            }
            if (branchId != null) {
                predicates.add(cb.equal(root.get("branch").get("id"), branchId));
            }
            if (paymentMethod != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod));
            }
            if (paymentStatusStr != null && !paymentStatusStr.isBlank()) {
                predicates.add(cb.equal(root.get("paymentStatus"), rw.madeleinegroup.common.enums.PaymentStatus.valueOf(paymentStatusStr.toUpperCase())));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordedAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordedAt"), dateTo));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
