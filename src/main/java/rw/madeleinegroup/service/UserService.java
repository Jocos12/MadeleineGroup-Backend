package rw.madeleinegroup.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.UserRequest;
import rw.madeleinegroup.dto.UserResponse;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final DeleteRequestRepository deleteRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, BranchRepository branchRepository,
                       DeleteRequestRepository deleteRequestRepository, PasswordEncoder passwordEncoder,
                       NotificationService notificationService, EmailService emailService) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.deleteRequestRepository = deleteRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<UserResponse> findByRole(Role role) {
        return userRepository.findByRole(role).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<UserResponse> findByBranch(Long branchId) {
        return userRepository.findByBranch_Id(branchId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return toResponse(user);
    }

    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return toResponse(user);
    }

    @Transactional
    public UserResponse create(UserRequest request, String createdByEmail) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }
        User creator = userRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));
        if (creator.getRole() != Role.CEO && creator.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only CEO or Admin can create users");
        }

        Branch branch = request.getBranchId() != null
                ? branchRepository.findById(request.getBranchId()).orElse(null) : null;

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .othersRoleSpecification(request.getRole() == Role.OTHERS ? request.getOthersRoleSpecification() : null)
                .branch(branch)
                .profilePhotoUrl(request.getProfilePhotoUrl())
                .enabled(true)
                .emailVerified(false)
                .build();
        user.setCreatedBy(creator);
        user = userRepository.save(user);
        notificationService.notifyUserCreated(user, creator);
        if (Boolean.TRUE.equals(request.getSendWelcomeEmail()) && request.getPassword() != null && !request.getPassword().isBlank()) {
            String dateStr = user.getCreatedAt() != null ? user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName(), creator.getFullName(), dateStr,
                    user.getEmail(), request.getPassword());
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request, String updaterEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        User updater = userRepository.findByEmail(updaterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Updater not found"));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getProfilePhotoUrl() != null) user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            if (updater.getRole() != Role.CEO && updater.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Only CEO or Admin can change roles");
            }
            user.setRole(request.getRole());
            user.setOthersRoleSpecification(request.getRole() == Role.OTHERS ? request.getOthersRoleSpecification() : null);
        }
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId()).orElse(null);
            user.setBranch(branch);
        }
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());

        user = userRepository.save(user);
        notificationService.notifyUserUpdated(user, updater);
        return toResponse(user);
    }

    /**
     * Request user deletion - requires CEO approval. Admin/Manager cannot delete without CEO approval.
     */
    @Transactional
    public void requestDelete(Long userId, String requesterEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        if (requester.getRole() == Role.CEO) {
            userRepository.delete(user);
            notificationService.notifyUserDeleted(user, requester);
            return;
        }

        if (requester.getRole() != Role.ADMIN && requester.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("Only CEO, Admin or Manager can request user deletion");
        }

        DeleteRequest deleteRequest = DeleteRequest.builder()
                .userToDelete(user)
                .requestedBy(requester)
                .status(DeleteRequestStatus.PENDING)
                .build();
        deleteRequestRepository.save(deleteRequest);
        notificationService.notifyDeleteRequested(deleteRequest);
    }

    /**
     * CEO approves or rejects delete request.
     */
    @Transactional
    public void approveDeleteRequest(Long deleteRequestId, boolean approve, String ceoEmail) {
        DeleteRequest dr = deleteRequestRepository.findById(deleteRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Delete request not found"));
        User ceo = userRepository.findByEmail(ceoEmail)
                .orElseThrow(() -> new ResourceNotFoundException("CEO not found"));
        if (ceo.getRole() != Role.CEO) {
            throw new IllegalArgumentException("Only CEO can approve delete requests");
        }

        dr.setStatus(approve ? DeleteRequestStatus.APPROVED : DeleteRequestStatus.REJECTED);
        dr.setApprovedBy(ceo);
        deleteRequestRepository.save(dr);

        if (approve) {
            userRepository.delete(dr.getUserToDelete());
            notificationService.notifyUserDeleted(dr.getUserToDelete(), ceo);
        } else {
            notificationService.notifyDeleteRejected(dr);
        }
    }

    @Transactional
    public UserResponse toggleStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setEnabled(!user.isEnabled());
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id, String deleterEmail) {
        User deleter = userRepository.findByEmail(deleterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Deleter not found"));
        if (deleter.getRole() == Role.CEO) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
            userRepository.delete(user);
            notificationService.notifyUserDeleted(user, deleter);
        } else {
            requestDelete(id, deleterEmail);
        }
    }

    private UserResponse toResponse(User u) {
        String createdByName = null;
        String createdByEmail = null;
        if (u.getCreatedBy() != null) {
            createdByName = u.getCreatedBy().getFullName();
            createdByEmail = u.getCreatedBy().getEmail();
        }
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .role(u.getRole())
                .othersRoleSpecification(u.getOthersRoleSpecification())
                .branchId(u.getBranch() != null ? u.getBranch().getId() : null)
                .branchName(u.getBranch() != null ? u.getBranch().getName() : null)
                .profilePhotoUrl(u.getProfilePhotoUrl())
                .enabled(u.isEnabled())
                .emailVerified(u.isEmailVerified())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .createdByName(createdByName)
                .createdByEmail(createdByEmail)
                .build();
    }
}
