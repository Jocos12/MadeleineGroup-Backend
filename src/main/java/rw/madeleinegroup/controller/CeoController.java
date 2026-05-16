package rw.madeleinegroup.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.common.ApiResponse;
import rw.madeleinegroup.common.enums.ExpenseStatus;
import rw.madeleinegroup.dto.*;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.repository.*;
import rw.madeleinegroup.service.*;
import rw.madeleinegroup.service.CustomUserDetailsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * CEO-only controller: Full CRUD for all entities, reports, analytics.
 */
@RestController
@RequestMapping("/api/ceo")
public class CeoController {

    private final ContactInquiryRepository contactInquiryRepository;
    private final ClientService clientService;
    private final BookingService bookingService;
    private final ClientExperienceService clientExperienceService;
    private final UserService userService;
    private final DeleteRequestRepository deleteRequestRepository;
    private final FinanceService financeService;
    private final PaymentService paymentService;
    private final PackageItemRepository packageItemRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final GalleryItemRepository galleryItemRepository;
    private final NotificationRepository notificationRepository;
    private final LoginAuditRepository loginAuditRepository;

    public CeoController(
            rw.madeleinegroup.repository.ContactInquiryRepository contactInquiryRepository,
            ClientService clientService,
            BookingService bookingService,
            ClientExperienceService clientExperienceService,
            UserService userService,
            rw.madeleinegroup.repository.DeleteRequestRepository deleteRequestRepository,
            FinanceService financeService,
            PaymentService paymentService,
            rw.madeleinegroup.repository.PackageItemRepository packageItemRepository,
            rw.madeleinegroup.repository.BranchRepository branchRepository,
            rw.madeleinegroup.repository.DepartmentRepository departmentRepository,
            rw.madeleinegroup.repository.GalleryItemRepository galleryItemRepository,
            rw.madeleinegroup.repository.NotificationRepository notificationRepository,
            rw.madeleinegroup.repository.LoginAuditRepository loginAuditRepository) {
        this.contactInquiryRepository = contactInquiryRepository;
        this.clientService = clientService;
        this.bookingService = bookingService;
        this.clientExperienceService = clientExperienceService;
        this.userService = userService;
        this.deleteRequestRepository = deleteRequestRepository;
        this.financeService = financeService;
        this.paymentService = paymentService;
        this.packageItemRepository = packageItemRepository;
        this.branchRepository = branchRepository;
        this.departmentRepository = departmentRepository;
        this.galleryItemRepository = galleryItemRepository;
        this.notificationRepository = notificationRepository;
        this.loginAuditRepository = loginAuditRepository;
    }

    private static ExpenseStatus parseExpenseStatus(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return ExpenseStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String email(CustomUserDetailsService.UserPrincipal p) {
        return p != null ? p.getEmail() : null;
    }

    // ─── CONTACT INQUIRIES ─────────────────────────────────────
    @GetMapping("/contact-inquiries")
    public ResponseEntity<?> listContactInquiries(@RequestParam(defaultValue = "50") int size) {
        var page = org.springframework.data.domain.PageRequest.of(0, Math.min(size, 100));
        List<ContactInquiry> list = contactInquiryRepository.findAllByOrderByCreatedAtDesc(page).getContent();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/contact-inquiries/{id}/read")
    public ResponseEntity<?> markContactRead(@PathVariable Long id) {
        ContactInquiry c = contactInquiryRepository.findById(id)
                .orElseThrow(() -> new rw.madeleinegroup.exception.ResourceNotFoundException("Contact inquiry not found"));
        c.setIsRead(true);
        contactInquiryRepository.save(c);
        return ResponseEntity.ok(ApiResponse.success(c, "Marked as read"));
    }

    @DeleteMapping("/contact-inquiries/{id}")
    public ResponseEntity<?> deleteContactInquiry(@PathVariable Long id) {
        contactInquiryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Contact inquiry deleted"));
    }

    // ─── CLIENTS CRUD ───────────────────────────────────────────
    @PostMapping("/clients")
    public ResponseEntity<?> createClient(@RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        String fullName = (String) body.get("fullName");
        String email = (String) body.get("email");
        String phone = body.get("phone") != null ? (String) body.get("phone") : null;
        String address = body.get("address") != null ? (String) body.get("address") : null;
        String notes = body.get("notes") != null ? (String) body.get("notes") : null;
        Long branchId = body.get("branchId") != null ? ((Number) body.get("branchId")).longValue() : null;
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Full name is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        rw.madeleinegroup.entity.Client client = clientService.createClient(
                fullName.trim(), email.trim(), phone, address, notes, branchId, email(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.toResponse(client));
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long branchId = null;
        if (body.containsKey("branchId")) {
            Object b = body.get("branchId");
            branchId = (b != null && b instanceof Number) ? ((Number) b).longValue() : null;
        }
        rw.madeleinegroup.entity.Client client = clientService.updateClient(
                id,
                (String) body.get("fullName"),
                (String) body.get("phone"),
                (String) body.get("address"),
                (String) body.get("notes"),
                (String) body.get("profilePhotoUrl"),
                branchId
        );
        return ResponseEntity.ok(clientService.toResponse(client));
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Client deleted"));
    }

    // ─── BOOKINGS CRUD ──────────────────────────────────────────
    @PutMapping("/bookings/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @Valid @RequestBody BookingRequest request,
                                           @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        Long modifierId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(bookingService.updateBooking(id, request, modifierId));
    }

    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestParam String status,
                                                 @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        Long modifierId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(bookingService.updateStatus(id, BookingStatus.valueOf(status), modifierId));
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking deleted"));
    }

    // ─── CLIENT EXPERIENCES ─────────────────────────────────────
    @GetMapping("/client-experiences")
    public ResponseEntity<?> listAllExperiences() {
        return ResponseEntity.ok(clientExperienceService.getAllExperiences());
    }

    @DeleteMapping("/client-experiences/{id}")
    public ResponseEntity<?> deleteExperience(@PathVariable Long id) {
        clientExperienceService.deleteExperience(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Experience deleted"));
    }

    // ─── USERS CRUD ─────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request,
                                         @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        rw.madeleinegroup.dto.UserResponse u = userService.create(request, email(p));
        return ResponseEntity.status(HttpStatus.CREATED).body(u);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request,
                                        @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        rw.madeleinegroup.dto.UserResponse u = userService.update(id, request, email(p));
        return ResponseEntity.ok(u);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                         @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        userService.delete(id, email(p));
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }

    // ─── DELETE REQUESTS ────────────────────────────────────────
    @GetMapping("/delete-requests")
    public ResponseEntity<?> listDeleteRequests(@RequestParam(required = false) String status) {
        List<DeleteRequest> list = status != null && !status.isBlank()
                ? deleteRequestRepository.findByStatusOrderByRequestedAtDesc(
                        DeleteRequestStatus.valueOf(status.trim().toUpperCase()))
                : deleteRequestRepository.findAllByOrderByRequestedAtDesc();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/delete-requests/{id}/approve")
    public ResponseEntity<?> approveDeleteRequest(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        userService.approveDeleteRequest(id, true, email(p));
        return ResponseEntity.ok(ApiResponse.success(null, "Delete request approved"));
    }

    @PostMapping("/delete-requests/{id}/reject")
    public ResponseEntity<?> rejectDeleteRequest(@PathVariable Long id,
                                                 @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        userService.approveDeleteRequest(id, false, email(p));
        return ResponseEntity.ok(ApiResponse.success(null, "Delete request rejected"));
    }

    // ─── FINANCE: Payments & Expenses ───────────────────────────
    @GetMapping("/payments")
    public ResponseEntity<?> listPayments(@RequestParam(required = false) Long branchId,
                                         @RequestParam(required = false) String query,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String paymentMethod,
                                         @RequestParam(required = false) String paymentStatus,
                                         @RequestParam(required = false) java.time.LocalDate dateFrom,
                                         @RequestParam(required = false) java.time.LocalDate dateTo,
                                         @RequestParam(required = false) String sortBy,
                                         @RequestParam(required = false) String sortDir,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        boolean hasSearch = (query != null && !query.isBlank()) || (type != null && !type.isBlank())
                || branchId != null || paymentMethod != null || paymentStatus != null
                || dateFrom != null || dateTo != null || (sortBy != null && !sortBy.isBlank());
        if (hasSearch) {
            return ResponseEntity.ok(financeService.searchPayments(query, type, branchId, paymentMethod, paymentStatus, dateFrom, dateTo, sortBy != null ? sortBy : "recordedAt", sortDir != null ? sortDir : "desc", page, size));
        }
        return ResponseEntity.ok(financeService.listAllPayments(branchId));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getPaymentById(id));
    }

    @PostMapping("/payments")
    public ResponseEntity<?> recordPayment(@Valid @RequestBody PaymentRequest request,
                                          @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        Payment pay = paymentService.recordPayment(request, email(p));
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(pay), "Payment recorded"));
    }

    @PutMapping("/payments/{id}")
    public ResponseEntity<?> updatePayment(@PathVariable Long id, @RequestBody PaymentUpdateRequest request,
                                           @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        return ResponseEntity.ok(financeService.updatePayment(id, request, email(principal)));
    }

    @DeleteMapping("/payments/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Long id) {
        financeService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expenses")
    public ResponseEntity<?> listExpenses(@RequestParam(required = false) Long branchId,
                                         @RequestParam(required = false) String status) {
        return ResponseEntity.ok(financeService.listAllExpenses(branchId, parseExpenseStatus(status)));
    }

    @PostMapping("/expenses")
    public ResponseEntity<?> recordExpense(@Valid @RequestBody ExpenseRequest request,
                                           @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        Object exp = financeService.recordExpense(request, email(p));
        return ResponseEntity.ok(ApiResponse.success(exp, "Expense recorded"));
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @RequestBody ExpenseUpdateRequest request,
                                           @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        rw.madeleinegroup.entity.Expense exp = financeService.updateExpense(id, request, email(principal));
        return ResponseEntity.ok(exp);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        financeService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted"));
    }

    @PostMapping("/expenses/{id}/first-approve")
    public ResponseEntity<?> firstApproveExpense(@PathVariable Long id,
                                                 @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        return ResponseEntity.ok(ApiResponse.success(financeService.firstApprove(id, email(p)), "First approval recorded"));
    }

    @PostMapping("/expenses/{id}/second-approve")
    public ResponseEntity<?> secondApproveExpense(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        return ResponseEntity.ok(ApiResponse.success(financeService.secondApprove(id, email(p)), "Expense finalized as paid"));
    }

    @PostMapping("/expenses/{id}/reject")
    public ResponseEntity<?> rejectExpense(@PathVariable Long id,
                                           @RequestBody(required = false) ExpenseRejectRequest req,
                                           @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal p) {
        ExpenseRejectRequest body = req != null ? req : new ExpenseRejectRequest();
        return ResponseEntity.ok(ApiResponse.success(financeService.rejectExpense(id, body, email(p)), "Expense rejected"));
    }

    @GetMapping("/finance/summary")
    public ResponseEntity<?> financeSummary(@RequestParam int year, @RequestParam(required = false) Integer month) {
        Object summary = month != null
                ? financeService.getGroupFinance(year, month)
                : financeService.getYearlySummary(year);
        return ResponseEntity.ok(ApiResponse.success(summary, "OK"));
    }

    // ─── PACKAGE ITEMS CRUD ─────────────────────────────────────
    @GetMapping("/packages")
    public ResponseEntity<?> listPackages(@RequestParam(required = false) Long branchId) {
        List<PackageItem> list = branchId != null
                ? packageItemRepository.findByBranch(branchRepository.findById(branchId).orElseThrow())
                : packageItemRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/packages")
    public ResponseEntity<?> createPackage(@RequestBody Map<String, Object> body) {
        if (body.get("price") == null) throw new IllegalArgumentException("Price is required");
        Branch branch = body.get("branchId") != null
                ? branchRepository.findById(Long.valueOf(body.get("branchId").toString()))
                        .orElse(branchRepository.findAll().stream().findFirst().orElse(null))
                : branchRepository.findAll().stream().findFirst().orElse(null);
        Department department = body.get("departmentId") != null
                ? departmentRepository.findById(Long.valueOf(body.get("departmentId").toString())).orElse(null)
                : null;
        PackageItem pkg = PackageItem.builder()
                .branch(branch)
                .department(department)
                .name((String) body.get("name"))
                .description((String) body.get("description"))
                .price(new java.math.BigDecimal(body.get("price").toString()))
                .pricingType(body.get("pricingType") != null ? PricingType.valueOf(body.get("pricingType").toString()) : PricingType.FIXED)
                .minGuests(body.get("minGuests") != null ? ((Number) body.get("minGuests")).intValue() : null)
                .maxGuests(body.get("maxGuests") != null ? ((Number) body.get("maxGuests")).intValue() : null)
                .priceUnit((String) body.get("priceUnit"))
                .category((String) body.get("category"))
                .isFeatured(body.get("isFeatured") != null ? (Boolean) body.get("isFeatured") : false)
                .build();
        pkg = packageItemRepository.save(pkg);
        return ResponseEntity.status(HttpStatus.CREATED).body(pkg);
    }

    @PutMapping("/packages/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PackageItem pkg = packageItemRepository.findById(id)
                .orElseThrow(() -> new rw.madeleinegroup.exception.ResourceNotFoundException("Package not found"));
        if (body.containsKey("branchId")) {
            pkg.setBranch(branchRepository.findById(Long.valueOf(body.get("branchId").toString())).orElse(pkg.getBranch()));
        }
        if (body.containsKey("departmentId")) {
            pkg.setDepartment(body.get("departmentId") != null
                    ? departmentRepository.findById(Long.valueOf(body.get("departmentId").toString())).orElse(pkg.getDepartment())
                    : null);
        }
        if (body.containsKey("name")) pkg.setName((String) body.get("name"));
        if (body.containsKey("description")) pkg.setDescription((String) body.get("description"));
        if (body.containsKey("price")) pkg.setPrice(new java.math.BigDecimal(body.get("price").toString()));
        if (body.containsKey("pricingType")) pkg.setPricingType(PricingType.valueOf(body.get("pricingType").toString()));
        if (body.containsKey("minGuests")) pkg.setMinGuests(body.get("minGuests") != null ? ((Number) body.get("minGuests")).intValue() : null);
        if (body.containsKey("maxGuests")) pkg.setMaxGuests(body.get("maxGuests") != null ? ((Number) body.get("maxGuests")).intValue() : null);
        if (body.containsKey("priceUnit")) pkg.setPriceUnit((String) body.get("priceUnit"));
        if (body.containsKey("category")) pkg.setCategory((String) body.get("category"));
        if (body.containsKey("isFeatured")) pkg.setIsFeatured((Boolean) body.get("isFeatured"));
        return ResponseEntity.ok(packageItemRepository.save(pkg));
    }

    @DeleteMapping("/packages/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Long id) {
        packageItemRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Package deleted"));
    }

    // ─── GALLERY ITEMS CRUD ─────────────────────────────────────
    @GetMapping("/gallery")
    public ResponseEntity<?> listGallery(@RequestParam(required = false) Long branchId) {
        List<GalleryItem> list = branchId != null
                ? galleryItemRepository.findByBranch_Id(branchId)
                : galleryItemRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/gallery")
    public ResponseEntity<?> createGalleryItem(@RequestBody Map<String, Object> body) {
        Branch branch = body.get("branchId") != null
                ? branchRepository.findById(Long.valueOf(body.get("branchId").toString())).orElse(null)
                : null;
        GalleryItem g = GalleryItem.builder()
                .imageUrl((String) body.get("imageUrl"))
                .altText((String) body.get("altText"))
                .category((String) body.getOrDefault("category", "general"))
                .isVideo(body.get("isVideo") != null ? (Boolean) body.get("isVideo") : false)
                .branch(branch)
                .build();
        g = galleryItemRepository.save(g);
        return ResponseEntity.status(HttpStatus.CREATED).body(g);
    }

    @PutMapping("/gallery/{id}")
    public ResponseEntity<?> updateGalleryItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        GalleryItem g = galleryItemRepository.findById(id)
                .orElseThrow(() -> new rw.madeleinegroup.exception.ResourceNotFoundException("Gallery item not found"));
        if (body.containsKey("imageUrl")) g.setImageUrl((String) body.get("imageUrl"));
        if (body.containsKey("altText")) g.setAltText((String) body.get("altText"));
        if (body.containsKey("category")) g.setCategory((String) body.get("category"));
        if (body.containsKey("isVideo")) g.setIsVideo((Boolean) body.get("isVideo"));
        if (body.containsKey("branchId")) {
            g.setBranch(body.get("branchId") != null
                    ? branchRepository.findById(Long.valueOf(body.get("branchId").toString())).orElse(null)
                    : null);
        }
        return ResponseEntity.ok(galleryItemRepository.save(g));
    }

    @DeleteMapping("/gallery/{id}")
    public ResponseEntity<?> deleteGalleryItem(@PathVariable Long id) {
        galleryItemRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Gallery item deleted"));
    }

    // ─── NOTIFICATIONS ──────────────────────────────────────────
    @GetMapping("/notifications")
    public ResponseEntity<?> listNotifications(@RequestParam(defaultValue = "50") int limit) {
        var list = notificationRepository.findFirst50ByOrderByCreatedAtDesc();
        return ResponseEntity.ok(list);
    }

    // ─── BRANCHES CRUD ─────────────────────────────────────────
    @PostMapping("/branches")
    public ResponseEntity<?> createBranch(@RequestBody Map<String, Object> body) {
        Branch b = new Branch();
        b.setCode(str(body, "code"));
        b.setName(str(body, "name"));
        b.setDescription(str(body, "description"));
        b.setAddress(str(body, "address"));
        b.setPhone(str(body, "phone"));
        b.setEmail(str(body, "email"));
        b.setManagerName(str(body, "managerName"));
        if (body.containsKey("active")) {
            b.setActive(bool(body, "active", true));
        }
        b = branchRepository.save(b);
        return ResponseEntity.status(HttpStatus.CREATED).body(b);
    }

    @PutMapping("/branches/{id}")
    public ResponseEntity<?> updateBranch(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new rw.madeleinegroup.exception.ResourceNotFoundException("Branch not found"));
        if (body.containsKey("code")) b.setCode(str(body, "code"));
        if (body.containsKey("name")) b.setName(str(body, "name"));
        if (body.containsKey("description")) b.setDescription(str(body, "description"));
        if (body.containsKey("address")) b.setAddress(str(body, "address"));
        if (body.containsKey("phone")) b.setPhone(str(body, "phone"));
        if (body.containsKey("email")) b.setEmail(str(body, "email"));
        if (body.containsKey("managerName")) b.setManagerName(str(body, "managerName"));
        if (body.containsKey("active")) b.setActive(bool(body, "active", b.isActive()));
        return ResponseEntity.ok(branchRepository.save(b));
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable Long id) {
        branchRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Branch deleted"));
    }

    // ─── RECENT LOGINS ──────────────────────────────────────────
    @GetMapping("/recent-logins")
    public ResponseEntity<List<RecentLoginDto>> recentLogins(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "15") int limit
    ) {
        String roleParam = (role == null || role.isBlank()) ? null : role.trim();
        String q = (search == null || search.isBlank()) ? null : search.trim();
        LocalDateTime fromDt = from == null ? null : from.atStartOfDay();
        LocalDateTime toDt = to == null ? null : to.atTime(23, 59, 59, 999_999_000);
        int lim = Math.min(Math.max(limit, 1), 15);
        var pageable = PageRequest.of(0, lim);
        List<LoginAudit> rows = loginAuditRepository.findRecentFiltered(roleParam, fromDt, toDt, q, pageable);
        return ResponseEntity.ok(rows.stream().map(RecentLoginDto::from).toList());
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static boolean bool(Map<String, Object> body, String key, boolean def) {
        if (!body.containsKey(key) || body.get(key) == null) return def;
        Object v = body.get(key);
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
