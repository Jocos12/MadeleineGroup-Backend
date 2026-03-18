package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.ContactRequest;
import rw.madeleinegroup.entity.ContactInquiry;
import rw.madeleinegroup.repository.ContactInquiryRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactInquiryRepository contactRepository;

    public ContactController(ContactInquiryRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitContact(@Valid @RequestBody ContactRequest request) {
        ContactInquiry inquiry = ContactInquiry.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .build();
        contactRepository.save(inquiry);
        return ResponseEntity.ok(Map.of("message", "Message sent successfully"));
    }
}
