package rw.madeleinegroup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.madeleinegroup.entity.ContactInquiry;

import java.util.List;

public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, Long> {

    List<ContactInquiry> findAllByOrderByCreatedAtDesc();

    Page<ContactInquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ContactInquiry> findByReadOrderByCreatedAtDesc(boolean read);

    List<ContactInquiry> findByRepliedOrderByCreatedAtDesc(boolean replied);

    long countByReadFalse();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE contact_inquiries SET is_read = 1 WHERE is_read = 0", nativeQuery = true)
    int markAllAsReadNative();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM contact_inquiries WHERE is_read = 1", nativeQuery = true)
    int deleteAllReadNative();

    @Query("SELECT c FROM ContactInquiry c WHERE "
            + "LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.subject) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.message) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "ORDER BY c.createdAt DESC")
    List<ContactInquiry> search(@Param("q") String q);
}
