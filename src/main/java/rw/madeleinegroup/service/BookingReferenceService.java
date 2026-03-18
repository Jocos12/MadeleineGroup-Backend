package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.repository.BookingReferenceSequenceRepository;

import java.time.LocalDate;

@Service
public class BookingReferenceService {

    private final BookingReferenceSequenceRepository refSeqRepository;

    public BookingReferenceService(BookingReferenceSequenceRepository refSeqRepository) {
        this.refSeqRepository = refSeqRepository;
    }

    /**
     * Generates the next unique booking reference (e.g. MG-2026-0001).
     * Uses atomic INSERT ON DUPLICATE KEY UPDATE so the database handles
     * concurrency - only one thread can get each reference.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNext() {
        int year = LocalDate.now().getYear();
        refSeqRepository.upsertAndIncrement(year);
        Long lastNumber = refSeqRepository.getLastNumber(year);
        return "MG-" + year + "-" + String.format("%04d", lastNumber);
    }
}
