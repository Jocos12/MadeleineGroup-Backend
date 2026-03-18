package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.dto.DepartmentResponse;
import rw.madeleinegroup.dto.PackageItemResponse;
import rw.madeleinegroup.entity.Department;
import rw.madeleinegroup.entity.PackageItem;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.DepartmentRepository;
import rw.madeleinegroup.repository.PackageItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final PackageItemRepository packageItemRepository;

    public DepartmentService(DepartmentRepository departmentRepository, PackageItemRepository packageItemRepository) {
        this.departmentRepository = departmentRepository;
        this.packageItemRepository = packageItemRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartmentsWithPackages() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponseWithPackages)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PackageItemResponse> getPackagesByDepartment(Long departmentId, Integer guestCount) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        List<PackageItem> packages;
        if (guestCount != null && guestCount > 0) {
            packages = packageItemRepository.findByDepartmentIdAndGuestCount(departmentId, guestCount);
        } else {
            packages = packageItemRepository.findByDepartment(dept);
        }
        return packages.stream().map(this::toPackageResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        return toResponseWithPackages(dept);
    }

    private DepartmentResponse toResponseWithPackages(Department d) {
        List<PackageItemResponse> pkgs = packageItemRepository.findByDepartment(d).stream()
                .map(this::toPackageResponse)
                .collect(Collectors.toList());
        DepartmentResponse resp = new DepartmentResponse();
        resp.setId(d.getId());
        resp.setCode(d.getCode());
        resp.setName(d.getName());
        resp.setDescription(d.getDescription());
        resp.setPackages(pkgs);
        return resp;
    }

    private PackageItemResponse toPackageResponse(PackageItem p) {
        PackageItemResponse pr = new PackageItemResponse();
        pr.setId(p.getId());
        pr.setName(p.getName());
        pr.setDescription(p.getDescription());
        pr.setPrice(p.getPrice());
        pr.setPricingType(p.getPricingType());
        pr.setMinGuests(p.getMinGuests());
        pr.setMaxGuests(p.getMaxGuests());
        pr.setPriceUnit(p.getPriceUnit());
        return pr;
    }
}
