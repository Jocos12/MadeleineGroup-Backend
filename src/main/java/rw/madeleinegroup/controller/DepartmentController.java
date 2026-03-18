package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.DepartmentResponse;
import rw.madeleinegroup.dto.PackageItemResponse;
import rw.madeleinegroup.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartmentsWithPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/{id}/packages")
    public ResponseEntity<List<PackageItemResponse>> getPackages(
            @PathVariable Long id,
            @RequestParam(required = false) Integer guestCount) {
        return ResponseEntity.ok(departmentService.getPackagesByDepartment(id, guestCount));
    }
}
