package rw.madeleinegroup.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.madeleinegroup.entity.BookingReferenceSequence;

import java.util.Optional;

@Repository
public interface BookingReferenceSequenceRepository extends JpaRepository<BookingReferenceSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BookingReferenceSequence s WHERE s.year = :year")
    Optional<BookingReferenceSequence> findByYearForUpdate(@Param("year") int year);

    /** Atomic: INSERT or UPDATE. Ensures row exists and increments last_number. */
    @Modifying
    @Query(value = "INSERT INTO booking_reference_sequence (year, last_number, version) VALUES (:year, 1, 0) " +
            "ON DUPLICATE KEY UPDATE last_number = last_number + 1", nativeQuery = true)
    void upsertAndIncrement(@Param("year") int year);

    @Query(value = "SELECT last_number FROM booking_reference_sequence WHERE year = :year", nativeQuery = true)
    Long getLastNumber(@Param("year") int year);
}
