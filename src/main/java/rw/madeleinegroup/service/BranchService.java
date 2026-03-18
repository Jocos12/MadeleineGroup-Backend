package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.BranchRequest;
import rw.madeleinegroup.dto.BranchResponse;
import rw.madeleinegroup.entity.Branch;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.BranchRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<BranchResponse> findAll() {
        return branchRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BranchResponse findById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + id));
        return toResponse(branch);
    }

    public BranchResponse findByCode(String code) {
        Branch branch = branchRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + code));
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse create(BranchRequest request) {
        if (branchRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Branch with code " + request.getCode() + " already exists");
        }
        Branch branch = Branch.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
        branch = branchRepository.save(branch);
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + id));
        if (request.getName() != null) branch.setName(request.getName());
        if (request.getDescription() != null) branch.setDescription(request.getDescription());
        branch = branchRepository.save(branch);
        return toResponse(branch);
    }

    @Transactional
    public void delete(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + id));
        branchRepository.delete(branch);
    }

    private BranchResponse toResponse(Branch b) {
        return BranchResponse.builder()
                .id(b.getId())
                .code(b.getCode())
                .name(b.getName())
                .description(b.getDescription())
                .build();
    }
}
