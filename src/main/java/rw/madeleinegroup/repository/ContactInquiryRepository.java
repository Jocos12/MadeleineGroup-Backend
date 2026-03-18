package rw.madeleinegroup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.ContactInquiry;

public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, Long> {

    Page<ContactInquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
