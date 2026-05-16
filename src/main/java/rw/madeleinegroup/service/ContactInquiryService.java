package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.ContactInquiryDto;
import rw.madeleinegroup.dto.ContactInquiryRequest;
import rw.madeleinegroup.entity.ContactInquiry;
import rw.madeleinegroup.repository.ContactInquiryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactInquiryService {

    private final ContactInquiryRepository contactInquiryRepository;
    private final EmailService emailService;

    public ContactInquiryService(ContactInquiryRepository contactInquiryRepository, EmailService emailService) {
        this.contactInquiryRepository = contactInquiryRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<ContactInquiryDto> getAll(Boolean read, Boolean replied) {
        List<ContactInquiry> list = contactInquiryRepository.findAllByOrderByCreatedAtDesc();
        return list.stream()
                .filter(c -> read == null || c.isRead() == read)
                .filter(c -> replied == null || c.isReplied() == replied)
                .map(ContactInquiryDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContactInquiryDto getById(Long id) {
        ContactInquiry c = contactInquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact inquiry not found"));
        return ContactInquiryDto.from(c);
    }

    @Transactional
    public ContactInquiryDto create(ContactInquiryRequest request) {
        ContactInquiry c = new ContactInquiry();
        c.setName(request.getName().trim());
        c.setEmail(request.getEmail().trim());
        c.setSubject(request.getSubject().trim());
        c.setMessage(request.getMessage().trim());
        c.setRead(false);
        c.setReplied(false);
        ContactInquiry saved = contactInquiryRepository.save(c);
        return ContactInquiryDto.from(saved);
    }

    @Transactional
    public ContactInquiryDto markAsRead(Long id) {
        ContactInquiry c = contactInquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact inquiry not found"));
        c.setRead(true);
        return ContactInquiryDto.from(contactInquiryRepository.save(c));
    }

    @Transactional
    public ContactInquiryDto markAsUnread(Long id) {
        ContactInquiry c = contactInquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact inquiry not found"));
        c.setRead(false);
        return ContactInquiryDto.from(contactInquiryRepository.save(c));
    }

    @Transactional
    public void markAllAsRead() {
        contactInquiryRepository.markAllAsReadNative();
    }

    @Transactional
    public ContactInquiryDto reply(Long id, String replyMessage, String repliedByName) {
        ContactInquiry c = contactInquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact inquiry not found"));
        String msg = replyMessage != null ? replyMessage.trim() : "";
        if (msg.isEmpty()) {
            throw new IllegalArgumentException("Reply message is required");
        }
        c.setReplyMessage(msg);
        c.setReplied(true);
        c.setRead(true);
        c.setRepliedAt(LocalDateTime.now());
        ContactInquiry saved = contactInquiryRepository.save(c);
        emailService.sendContactInquiryReplyEmail(
                saved.getEmail(),
                saved.getSubject(),
                saved.getName(),
                saved.getMessage(),
                msg,
                repliedByName != null ? repliedByName : "Madeleine Group"
        );
        return ContactInquiryDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!contactInquiryRepository.existsById(id)) {
            throw new IllegalArgumentException("Contact inquiry not found");
        }
        contactInquiryRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllRead() {
        contactInquiryRepository.deleteAllReadNative();
    }

    @Transactional(readOnly = true)
    public List<ContactInquiryDto> search(String query) {
        if (query == null || query.isBlank()) {
            return contactInquiryRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(ContactInquiryDto::from)
                    .collect(Collectors.toList());
        }
        String q = query.trim();
        return contactInquiryRepository.search(q).stream()
                .map(ContactInquiryDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return contactInquiryRepository.countByReadFalse();
    }
}
