package rw.madeleinegroup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.madeleinegroup.dto.AnnouncementDto;
import rw.madeleinegroup.dto.AnnouncementRequest;
import rw.madeleinegroup.entity.Announcement;
import rw.madeleinegroup.entity.AnnouncementAudience;
import rw.madeleinegroup.entity.Role;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.AnnouncementRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Value("${app.api-base-url:http://localhost:8082}")
    private String apiBaseUrl;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                                 UserRepository userRepository,
                                 FileStorageService fileStorageService,
                                 EmailService emailService) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> listVisibleFor(User reader) {
        if (reader == null || reader.getRole() == null) {
            return List.of();
        }
        Role r = reader.getRole();
        return announcementRepository.findActiveWithAuthors().stream()
                .filter(a -> canSee(a.getAudience(), r))
                .map(AnnouncementDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> listAllForManagement() {
        return announcementRepository.findAllWithAuthors().stream()
                .map(AnnouncementDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementDto create(AnnouncementRequest req, Long authorId) {
        User author = userRepository.findById(authorId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Announcement a = new Announcement();
        a.setTitle(req.getTitle().trim());
        a.setBody(req.getBody().trim());
        a.setAudience(req.getAudience());
        a.setActive(req.getActive() == null || req.getActive());
        a.setCreatedBy(author);
        if (req.getImageUrls() != null) {
            a.setImageUrls(new ArrayList<>(req.getImageUrls()));
        }
        Announcement saved = announcementRepository.save(a);
        if (Boolean.TRUE.equals(req.getSendNotificationEmail())) {
            List<String> emails = resolveRecipientEmails(saved.getAudience());
            saved.setEmailNotifiedCount(emails.size());
            saved = announcementRepository.save(saved);
            emailService.sendAnnouncementBrandedEmails(
                    emails,
                    saved.getTitle(),
                    saved.getBody(),
                    formatAuthorName(author),
                    saved.getImageUrls(),
                    saved.getAudience() != null ? saved.getAudience().name().replace('_', ' ') : ""
            );
        }
        return AnnouncementDto.from(saved);
    }

    @Transactional
    public AnnouncementDto update(Long id, AnnouncementRequest req) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found"));
        a.setTitle(req.getTitle().trim());
        a.setBody(req.getBody().trim());
        a.setAudience(req.getAudience());
        if (req.getActive() != null) {
            a.setActive(req.getActive());
        }
        if (req.getImageUrls() != null) {
            a.setImageUrls(new ArrayList<>(req.getImageUrls()));
        }
        Announcement saved = announcementRepository.save(a);
        if (Boolean.TRUE.equals(req.getSendNotificationEmail())) {
            List<String> emails = resolveRecipientEmails(saved.getAudience());
            saved.setEmailNotifiedCount(emails.size());
            saved = announcementRepository.save(saved);
            User author = saved.getCreatedBy() != null ? saved.getCreatedBy() : null;
            emailService.sendAnnouncementBrandedEmails(
                    emails,
                    saved.getTitle(),
                    saved.getBody(),
                    author != null ? formatAuthorName(author) : "Madeleine Group",
                    saved.getImageUrls(),
                    saved.getAudience() != null ? saved.getAudience().name().replace('_', ' ') : ""
            );
        }
        return AnnouncementDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new IllegalArgumentException("Announcement not found");
        }
        announcementRepository.deleteById(id);
    }

    /**
     * Stores one image under {@code uploads/announcements/{id}/} and appends its public URL to the announcement.
     */
    @Transactional
    public AnnouncementDto addImage(Long announcementId, MultipartFile file) {
        Announcement a = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found"));
        String relative = fileStorageService.storeFile(file, "announcements/" + announcementId);
        String url = apiBaseUrl + "/uploads" + relative;
        if (a.getImageUrls() == null) {
            a.setImageUrls(new ArrayList<>());
        }
        a.getImageUrls().add(url);
        return AnnouncementDto.from(announcementRepository.save(a));
    }

    public static boolean canSee(AnnouncementAudience audience, Role role) {
        if (role == null || audience == null) {
            return false;
        }
        if (role == Role.CEO) {
            return true;
        }
        return switch (audience) {
            case EVERYONE -> true;
            case ALL_TEAM -> role == Role.CEO || role == Role.MANAGER || role == Role.ADMIN;
            case CEO, CEO_ONLY -> role == Role.CEO;
            case MANAGER -> role == Role.MANAGER;
            case ADMIN -> role == Role.ADMIN;
            case CLIENT -> role == Role.CLIENT;
            case OTHERS -> role == Role.OTHERS;
        };
    }

    private List<String> resolveRecipientEmails(AnnouncementAudience audience) {
        List<User> users = resolveRecipientUsers(audience);
        return users.stream()
                .map(User::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<User> resolveRecipientUsers(AnnouncementAudience audience) {
        if (audience == null) {
            return List.of();
        }
        return switch (audience) {
            case EVERYONE -> userRepository.findAll().stream()
                    .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                    .collect(Collectors.toList());
            case ALL_TEAM -> {
                Set<Long> seen = new HashSet<>();
                List<User> out = new ArrayList<>();
                for (Role r : List.of(Role.CEO, Role.MANAGER, Role.ADMIN)) {
                    for (User u : userRepository.findByRole(r)) {
                        if (u.getEmail() != null && !u.getEmail().isBlank() && seen.add(u.getId())) {
                            out.add(u);
                        }
                    }
                }
                yield out;
            }
            case CEO, CEO_ONLY -> withEmail(userRepository.findByRole(Role.CEO));
            case MANAGER -> withEmail(userRepository.findByRole(Role.MANAGER));
            case ADMIN -> withEmail(userRepository.findByRole(Role.ADMIN));
            case CLIENT -> withEmail(userRepository.findByRole(Role.CLIENT));
            case OTHERS -> withEmail(userRepository.findByRole(Role.OTHERS));
        };
    }

    private static List<User> withEmail(List<User> users) {
        return users.stream()
                .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                .collect(Collectors.toList());
    }

    private static String formatAuthorName(User author) {
        if (author.getFullName() != null && !author.getFullName().isBlank()) {
            return author.getFullName().trim();
        }
        if (author.getEmail() != null && !author.getEmail().isBlank()) {
            return author.getEmail().trim();
        }
        return "Madeleine Group";
    }
}
