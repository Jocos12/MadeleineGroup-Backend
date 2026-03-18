package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.BranchResponse;
import rw.madeleinegroup.service.BranchService;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public ResponseEntity<List<BranchResponse>> list() {
        return ResponseEntity.ok(branchService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.findById(id));
    }
}
