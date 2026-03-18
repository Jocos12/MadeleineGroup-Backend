package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.BlockedDateRequest;
import rw.madeleinegroup.dto.BlockedDateResponse;
import rw.madeleinegroup.entity.BlockedDate;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.BlockedDateRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlockedDateService {

    private final BlockedDateRepository blockedDateRepository;
    private final UserRepository userRepository;

    public BlockedDateService(BlockedDateRepository blockedDateRepository, UserRepository userRepository) {
        this.blockedDateRepository = blockedDateRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BlockedDateResponse> getAllBlockedDates() {
        return blockedDateRepository.findAllByOrderByBlockedDateAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BlockedDateResponse addBlockedDate(BlockedDateRequest request, Long adminUserId) {
        if (blockedDateRepository.existsByBlockedDate(request.getBlockedDate())) {
            throw new IllegalArgumentException("This date is already blocked");
        }
        BlockedDate bd = new BlockedDate();
        bd.setBlockedDate(request.getBlockedDate());
        bd.setReason(request.getReason());
        if (adminUserId != null) {
            User admin = userRepository.findById(adminUserId).orElse(null);
            if (admin != null) {
                bd.setBlockedBy(admin);
            }
        }
        bd = blockedDateRepository.save(bd);
        return toResponse(bd);
    }

    @Transactional
    public void removeBlockedDate(Long id) {
        BlockedDate bd = blockedDateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blocked date not found"));
        blockedDateRepository.delete(bd);
    }

    @Transactional(readOnly = true)
    public boolean isDateBlocked(LocalDate date) {
        return blockedDateRepository.existsByBlockedDate(date);
    }

    private BlockedDateResponse toResponse(BlockedDate bd) {
        BlockedDateResponse resp = new BlockedDateResponse();
        resp.setId(bd.getId());
        resp.setBlockedDate(bd.getBlockedDate());
        resp.setReason(bd.getReason());
        resp.setCreatedAt(bd.getCreatedAt());
        if (bd.getBlockedBy() != null) {
            resp.setBlockedByName(bd.getBlockedBy().getFullName());
        }
        return resp;
    }
}
