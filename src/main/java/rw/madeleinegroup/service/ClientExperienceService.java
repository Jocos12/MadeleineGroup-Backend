package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.ClientExperienceRequest;
import rw.madeleinegroup.dto.ClientExperienceResponse;
import rw.madeleinegroup.entity.ClientExperience;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.ClientExperienceRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientExperienceService {

    private final ClientExperienceRepository experienceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public ClientExperienceService(ClientExperienceRepository experienceRepository,
                                   UserRepository userRepository,
                                   NotificationService notificationService,
                                   EmailService emailService) {
        this.experienceRepository = experienceRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    public ClientExperience submitExperience(ClientExperienceRequest request, Long userId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        ClientExperience exp = ClientExperience.builder()
                .user(user)
                .authorName(request.getAuthorName())
                .authorEmail(request.getAuthorEmail())
                .comment(request.getComment())
                .rating(request.getRating())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .clientPhotoUrl(request.getClientPhotoUrl() != null ? request.getClientPhotoUrl() : request.getAuthorPhotoUrl())
                .approvalStatus(ClientExperience.ApprovalStatus.PENDING)
                .build();
        exp = experienceRepository.save(exp);
        notificationService.notifyNewClientExperience(exp);
        return exp;
    }

    @Transactional(readOnly = true)
    public List<ClientExperienceResponse> getApprovedExperiences() {
        return experienceRepository.findByApprovalStatus(ClientExperience.ApprovalStatus.APPROVED).stream()
                .map(ClientExperienceResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientExperienceResponse> getPendingExperiences() {
        return experienceRepository.findByApprovalStatus(ClientExperience.ApprovalStatus.PENDING).stream()
                .map(ClientExperienceResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ClientExperience approveExperience(Long id, String ceoEmail) {
        ClientExperience exp = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        User ceo = userRepository.findByEmail(ceoEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        exp.setApprovalStatus(ClientExperience.ApprovalStatus.APPROVED);
        exp.setApprovedAt(LocalDateTime.now());
        exp.setApprovedBy(ceo);
        exp = experienceRepository.save(exp);
        notificationService.notifyExperienceApproved(exp);
        String authorEmail = exp.getAuthorEmail();
        if (authorEmail != null && !authorEmail.isBlank()) {
            emailService.sendTestimonialApprovedEmail(authorEmail, exp.getAuthorName());
        }
        return exp;
    }

    @Transactional
    public ClientExperience rejectExperience(Long id, String ceoEmail) {
        return rejectWithReason(id, ceoEmail, null);
    }

    @Transactional
    public ClientExperience rejectWithReason(Long id, String ceoEmail, String rejectionReason) {
        ClientExperience exp = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        exp.setApprovalStatus(ClientExperience.ApprovalStatus.REJECTED);
        exp.setApprovedAt(LocalDateTime.now());
        exp.setApprovedBy(userRepository.findByEmail(ceoEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        if (rejectionReason != null && !rejectionReason.isBlank()) {
            exp.setRejectionReason(rejectionReason.trim());
        }
        return experienceRepository.save(exp);
    }

    @Transactional(readOnly = true)
    public List<ClientExperienceResponse> getAllExperiences() {
        return experienceRepository.findAll().stream()
                .map(ClientExperienceResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteExperience(Long id) {
        ClientExperience exp = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        experienceRepository.delete(exp);
    }

    @Transactional
    public ClientExperienceResponse updateExperience(Long id, ClientExperienceRequest request) {
        ClientExperience exp = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        if (request.getAuthorName() != null) exp.setAuthorName(request.getAuthorName());
        if (request.getAuthorEmail() != null) exp.setAuthorEmail(request.getAuthorEmail());
        if (request.getComment() != null) exp.setComment(request.getComment());
        if (request.getRating() != null) exp.setRating(request.getRating());
        if (request.getEventType() != null) exp.setEventType(request.getEventType());
        if (request.getEventDate() != null) exp.setEventDate(request.getEventDate());
        if (request.getClientPhotoUrl() != null) exp.setClientPhotoUrl(request.getClientPhotoUrl());
        else if (request.getAuthorPhotoUrl() != null) exp.setClientPhotoUrl(request.getAuthorPhotoUrl());
        exp = experienceRepository.save(exp);
        return ClientExperienceResponse.fromEntity(exp);
    }
}