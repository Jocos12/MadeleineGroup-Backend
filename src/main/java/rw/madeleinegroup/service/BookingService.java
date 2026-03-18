package rw.madeleinegroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.common.enums.PaymentMethod;
import rw.madeleinegroup.common.enums.PaymentStatus;
import rw.madeleinegroup.dto.*;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import rw.madeleinegroup.repository.BookingSpecification;

@Service
public class BookingService {

    private static final String PROTOCOL_DEPARTMENT_CODE = "PROTOCOL_SERVICES";

    private final BookingRepository bookingRepository;
    private final BranchRepository branchRepository;
    private final ClientRepository clientRepository;
    private final PackageItemRepository packageItemRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BlockedDateService blockedDateService;
    private final BookingReferenceService bookingReferenceService;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    public BookingService(BookingRepository bookingRepository, BranchRepository branchRepository,
                          ClientRepository clientRepository, PackageItemRepository packageItemRepository,
                          UserRepository userRepository, NotificationService notificationService,
                          BlockedDateService blockedDateService,
                          BookingReferenceService bookingReferenceService,
                          PaymentRepository paymentRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.branchRepository = branchRepository;
        this.clientRepository = clientRepository;
        this.packageItemRepository = packageItemRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.blockedDateService = blockedDateService;
        this.bookingReferenceService = bookingReferenceService;
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
    }

    /** Formats user as "FullName - Role" (e.g. "Sarah - Commercial" for OTHERS with specification). */
    private String formatUserSource(User user) {
        if (user == null) return null;
        String roleDisplay = (user.getRole() == Role.OTHERS && user.getOthersRoleSpecification() != null
                && !user.getOthersRoleSpecification().isBlank())
                ? user.getOthersRoleSpecification()
                : user.getRole().name();
        return user.getFullName() + " - " + roleDisplay;
    }

    private void validateDateAvailability(LocalDate eventDate, Long excludeBookingId) {
        if (blockedDateService.isDateBlocked(eventDate)) {
            throw new IllegalArgumentException("The selected date " + eventDate + " is not available for booking. Please choose another date.");
        }
        List<Booking> existing = bookingRepository.findByEventDateBetween(eventDate, eventDate);
        for (Booking b : existing) {
            if (excludeBookingId != null && b.getId().equals(excludeBookingId)) continue;
            if (b.getStatus() == BookingStatus.CONFIRMED
                    || b.getStatus() == BookingStatus.IN_PROGRESS
                    || b.getStatus() == BookingStatus.COMPLETED) {
                throw new IllegalArgumentException(
                        "The selected date " + eventDate + " already has a confirmed booking. Please choose another date."
                );
            }
        }
    }

    private void validateProtocolPackage(PackageItem pkg, int guestCount) {
        if (pkg.getDepartment() == null || !PROTOCOL_DEPARTMENT_CODE.equals(pkg.getDepartment().getCode())) return;
        boolean valid = (pkg.getMinGuests() == null || guestCount >= pkg.getMinGuests())
                && (pkg.getMaxGuests() == null || guestCount <= pkg.getMaxGuests());
        if (!valid) {
            String suggestion;
            if (guestCount <= 400) {
                suggestion = "For " + guestCount + " guests, please select the Standard Protocol package (up to 400 guests).";
            } else if (guestCount <= 500) {
                suggestion = "For " + guestCount + " guests, please select the Premium Protocol package (up to 500 guests).";
            } else {
                suggestion = "For " + guestCount + " guests, please select the Luxe Protocol package (for 500+ guests).";
            }
            throw new IllegalArgumentException("The selected Protocol package \"" + pkg.getName() + "\" is not valid for " + guestCount + " guests. " + suggestion);
        }
    }

    private BigDecimal calculateLineTotal(PackageItem pkg, int quantity, Integer guestCount) {
        PricingType type = pkg.getPricingType() != null ? pkg.getPricingType() : PricingType.FIXED;
        if (type == PricingType.PER_GUEST) {
            if (guestCount == null || guestCount <= 0) {
                throw new IllegalArgumentException("Guest count is required for Catering (per-guest) packages. Please enter the number of guests.");
            }
            return pkg.getPrice().multiply(BigDecimal.valueOf(guestCount)).multiply(BigDecimal.valueOf(quantity));
        }
        return pkg.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    private int getQuantityForPerGuest(PackageItem pkg) {
        return pkg.getPricingType() == PricingType.PER_GUEST ? 1 : 1;
    }

    @Transactional
    public Booking createBooking(BookingRequest request, Long creatorUserId) {
        if (request.getGuestCount() == null || request.getGuestCount() < 1 || request.getGuestCount() > 500) {
            throw new IllegalArgumentException("Guest count must be between 1 and 500");
        }
        validateDateAvailability(request.getEventDate(), null);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        User createdBy = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String source = createdBy.getRole() == Role.CLIENT
                ? "Client - Online"
                : formatUserSource(createdBy);

        String ref = bookingReferenceService.generateNext();
        Booking booking = Booking.builder()
                .bookingReference(ref)
                .client(client)
                .branch(branch)
                .status(BookingStatus.PENDING)
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .guestCount(request.getGuestCount())
                .notes(request.getNotes())
                .estimatedAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .createdBy(createdBy)
                .build();
        booking.setSource(source);

        List<Long> pkgIds = request.getPackageIds();
        int guestCount = request.getGuestCount();
        if (pkgIds != null && !pkgIds.isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (Long pkgId : pkgIds) {
                PackageItem pkg = packageItemRepository.findById(pkgId)
                        .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + pkgId));
                validateProtocolPackage(pkg, guestCount);
                PricingType pkgType = pkg.getPricingType() != null ? pkg.getPricingType() : PricingType.FIXED;
                int qty = 1;
                BigDecimal unitPrice = pkgType == PricingType.PER_GUEST
                        ? pkg.getPrice().multiply(BigDecimal.valueOf(guestCount))
                        : pkg.getPrice();
                BigDecimal lineTotal = calculateLineTotal(pkg, 1, guestCount);
                total = total.add(lineTotal);
                booking.getBookingPackages().add(BookingPackage.builder()
                        .booking(booking)
                        .packageItem(pkg)
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .build());
            }
            for (BookingPackage bp : booking.getBookingPackages()) {
                if (bp.getTotalPrice() == null && bp.getUnitPrice() != null && bp.getQuantity() != null) {
                    bp.setTotalPrice(bp.getUnitPrice().multiply(BigDecimal.valueOf(bp.getQuantity())));
                }
            }
            // If frontend sends a negotiated/confirmed amount, prefer it over the catalog total.
            if (request.getEstimatedAmount() != null && request.getEstimatedAmount().compareTo(BigDecimal.ZERO) > 0) {
                booking.setEstimatedAmount(request.getEstimatedAmount());
            } else {
                booking.setEstimatedAmount(total);
            }
        } else if (request.getEstimatedAmount() != null) {
            // No package list but explicit amount provided (edge case)
            booking.setEstimatedAmount(request.getEstimatedAmount());
        }

        booking = bookingRepository.save(booking);
        notificationService.notifyNewBooking(booking);
        return booking;
    }

    public List<Booking> getBookingsByBranch(Long branchId) {
        return bookingRepository.findByBranchIdWithDetails(branchId);
    }

    public List<Booking> getBookingsByClient(Long clientId) {
        return bookingRepository.findByClientIdWithDetails(clientId);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByClientAsResponse(Long clientId) {
        return getBookingsByClient(clientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForCalendar(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return bookingRepository.findByEventDateBetweenWithDetails(start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse updateAdminNotes(Long id, String adminNotes) {
        Booking booking = getById(id);
        booking.setAdminNotes(adminNotes);
        booking = bookingRepository.save(booking);
        return toResponse(booking);
    }

    public Booking getById(Long id) {
        return bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    public List<Booking> findAll(Long branchId, String status) {
        if (branchId != null && status != null) {
            return bookingRepository.findByBranchIdAndStatusWithDetails(branchId, BookingStatus.valueOf(status));
        }
        if (branchId != null) {
            return bookingRepository.findByBranchIdWithDetails(branchId);
        }
        if (status != null) {
            return bookingRepository.findByStatusWithDetails(BookingStatus.valueOf(status));
        }
        return bookingRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAllAsResponse(Long branchId, String status) {
        return findAll(branchId, status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Booking findById(Long id) {
        return getById(id);
    }

    @Transactional(readOnly = true)
    public BookingResponse findByIdAsResponse(Long id) {
        return toResponse(getById(id));
    }

    private BookingResponse toResponse(Booking b) {
        String clientName = null;
        String clientEmail = null;
        Long clientId = null;
        if (b.getClient() != null) {
            clientId = b.getClient().getId();
            clientName = b.getClient().getFullName();
            clientEmail = b.getClient().getEmail();
        }
        String branchName = null;
        Long branchId = null;
        if (b.getBranch() != null) {
            branchId = b.getBranch().getId();
            branchName = b.getBranch().getName();
        }
        Long createdById = null;
        String createdByName = null;
        if (b.getCreatedBy() != null) {
            createdById = b.getCreatedBy().getId();
            createdByName = b.getCreatedBy().getFullName();
        }
        List<BookingPackageResponse> pkgList = new ArrayList<>();
        if (b.getBookingPackages() != null) {
            for (BookingPackage bp : b.getBookingPackages()) {
                String pkgName = (bp.getPackageItem() != null) ? bp.getPackageItem().getName() : null;
                Long pkgId = (bp.getPackageItem() != null) ? bp.getPackageItem().getId() : null;
                BigDecimal total = bp.getTotalPrice();
                if (total == null && bp.getUnitPrice() != null && bp.getQuantity() != null) {
                    total = bp.getUnitPrice().multiply(BigDecimal.valueOf(bp.getQuantity()));
                }
                pkgList.add(BookingPackageResponse.builder()
                        .id(bp.getId())
                        .packageItemId(pkgId)
                        .packageName(pkgName)
                        .quantity(bp.getQuantity())
                        .unitPrice(bp.getUnitPrice())
                        .totalPrice(total)
                        .build());
            }
        }
        return BookingResponse.builder()
                .id(b.getId())
                .bookingReference(b.getBookingReference())
                .clientId(clientId)
                .clientName(clientName)
                .clientEmail(clientEmail)
                .branchId(branchId)
                .branchName(branchName)
                .status(b.getStatus())
                .eventType(b.getEventType())
                .eventDate(b.getEventDate())
                .guestCount(b.getGuestCount())
                .notes(b.getNotes())
                .adminNotes(b.getAdminNotes())
                .estimatedAmount(b.getEstimatedAmount())
                .paidAmount(b.getPaidAmount())
                .createdById(createdById)
                .createdByName(createdByName)
                .source(b.getSource())
                .lastModifiedBy(b.getLastModifiedBy())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .packages(pkgList)
                .build();
    }

    @Transactional
    public BookingResponse updateStatus(Long id, BookingStatus status, Long modifierUserId) {
        Booking booking = getById(id);
        if (status == BookingStatus.COMPLETED) {
            LocalDate today = LocalDate.now(java.time.ZoneId.of("Africa/Kigali"));
            if (booking.getEventDate() != null && booking.getEventDate().isAfter(today)) {
                throw new IllegalArgumentException(
                        "Cannot mark booking as COMPLETED — the event date " +
                                booking.getEventDate() + " has not passed yet."
                );
            }
        }
        booking.setStatus(status);
        if (modifierUserId != null) {
            User modifier = userRepository.findById(modifierUserId).orElse(null);
            if (modifier != null) {
                booking.setUpdatedBy(modifier);
                booking.setLastModifiedBy(formatUserSource(modifier));
            }
        }
        booking = bookingRepository.save(booking);
        notificationService.notifyBookingStatusUpdated(booking);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest request, Long modifierUserId) {
        Booking booking = getById(id);
        if (modifierUserId != null) {
            User modifier = userRepository.findById(modifierUserId).orElse(null);
            if (modifier != null) {
                booking.setUpdatedBy(modifier);
                booking.setLastModifiedBy(formatUserSource(modifier));
            }
        }
        if (request.getClientId() != null) {
            Client client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
            booking.setClient(client);
        }
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
            booking.setBranch(branch);
        }
        if (request.getEventType() != null) booking.setEventType(request.getEventType());
        if (request.getEventDate() != null) booking.setEventDate(request.getEventDate());
        if (request.getGuestCount() != null) booking.setGuestCount(request.getGuestCount());
        if (request.getNotes() != null) booking.setNotes(request.getNotes());
        if (request.getEstimatedAmount() != null) booking.setEstimatedAmount(request.getEstimatedAmount());

        if (request.getEventDate() != null && !request.getEventDate().equals(booking.getEventDate())) {
            validateDateAvailability(request.getEventDate(), booking.getId());
        }

        int guestCount = request.getGuestCount() != null ? request.getGuestCount() : booking.getGuestCount() != null ? booking.getGuestCount() : 0;
        booking.getBookingPackages().clear();
        List<Long> pkgIds = request.getPackageIds();
        if (pkgIds != null && !pkgIds.isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (Long pkgId : pkgIds) {
                PackageItem pkg = packageItemRepository.findById(pkgId)
                        .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + pkgId));
                validateProtocolPackage(pkg, guestCount);
                PricingType pkgType2 = pkg.getPricingType() != null ? pkg.getPricingType() : PricingType.FIXED;
                int qty = 1;
                BigDecimal unitPrice = pkgType2 == PricingType.PER_GUEST
                        ? pkg.getPrice().multiply(BigDecimal.valueOf(guestCount))
                        : pkg.getPrice();
                BigDecimal lineTotal = calculateLineTotal(pkg, 1, guestCount > 0 ? guestCount : null);
                total = total.add(lineTotal);
                BookingPackage bp = BookingPackage.builder()
                        .booking(booking)
                        .packageItem(pkg)
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .build();
                bp.setTotalPrice(lineTotal);
                booking.getBookingPackages().add(bp);
            }
            if (request.getEstimatedAmount() != null && request.getEstimatedAmount().compareTo(BigDecimal.ZERO) > 0) {
                booking.setEstimatedAmount(request.getEstimatedAmount());
            } else {
                booking.setEstimatedAmount(total);
            }
        } else if (request.getPackages() != null && !request.getPackages().isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (BookingPackageRequest bp : request.getPackages()) {
                PackageItem pkg = packageItemRepository.findById(bp.getPackageId())
                        .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + bp.getPackageId()));
                validateProtocolPackage(pkg, guestCount);
                PricingType pkgType3 = pkg.getPricingType() != null ? pkg.getPricingType() : PricingType.FIXED;
                int qty = bp.getQuantity() != null ? bp.getQuantity() : 1;
                BigDecimal lineTotal = calculateLineTotal(pkg, qty, guestCount > 0 ? guestCount : null);
                BigDecimal unitPrice = pkgType3 == PricingType.PER_GUEST
                        ? pkg.getPrice().multiply(BigDecimal.valueOf(guestCount))
                        : (bp.getUnitPrice() != null ? bp.getUnitPrice() : pkg.getPrice());
                total = total.add(lineTotal);
                BookingPackage bkp = BookingPackage.builder()
                        .booking(booking)
                        .packageItem(pkg)
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .build();
                bkp.setTotalPrice(lineTotal);
                booking.getBookingPackages().add(bkp);
            }
            if (request.getEstimatedAmount() != null && request.getEstimatedAmount().compareTo(BigDecimal.ZERO) > 0) {
                booking.setEstimatedAmount(request.getEstimatedAmount());
            } else {
                booking.setEstimatedAmount(total);
            }
        }
        booking = bookingRepository.save(booking);
        notificationService.notifyBookingUpdated(booking);
        return toResponse(booking);
    }

    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = getById(id);
        bookingRepository.delete(booking);
        notificationService.notifyBookingCancelled(booking);
    }

    @Transactional(readOnly = true)
    public List<LocalDate> getUnavailableDates(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        Set<LocalDate> unavailable = new HashSet<>();
        List<LocalDate> blocked = blockedDateService.getAllBlockedDates().stream()
                .map(BlockedDateResponse::getBlockedDate)
                .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                .toList();
        unavailable.addAll(blocked);
        List<LocalDate> booked = bookingRepository.findEventDatesBetween(start, end);
        unavailable.addAll(booked);
        return unavailable.stream().sorted().toList();
    }

    @Transactional(readOnly = true)
    public BookingSearchResponse searchBookings(String query, String statusStr, Long branchId,
                                                 String eventType, LocalDate dateFrom, LocalDate dateTo,
                                                 int page, int size) {
        BookingStatus status = (statusStr != null && !statusStr.isBlank())
                ? BookingStatus.valueOf(statusStr.toUpperCase()) : null;
        Specification<Booking> spec = BookingSpecification.searchBookings(query, status, branchId, eventType, dateFrom, dateTo);
        Page<Booking> pageResult = bookingRepository.findAll(spec, PageRequest.of(page, size));
        List<BookingResponse> content = pageResult.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new BookingSearchResponse(content, pageResult.getTotalElements(),
                pageResult.getTotalPages(), page, size);
    }

    @Transactional
    public BookingConfirmationResponse confirmBooking(Long id, BookingConfirmationRequest request, Long userId) {
        Booking booking = getById(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be confirmed. Current status: " + booking.getStatus());
        }
        BigDecimal estimated = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO;
        User modifier = userId != null ? userRepository.findById(userId).orElse(null) : null;
        if (modifier != null) {
            booking.setUpdatedBy(modifier);
            booking.setLastModifiedBy(formatUserSource(modifier));
        }
        PaymentMethod method = request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH;
        String notes = request.getPaymentNotes();

        BookingConfirmationResponse resp = new BookingConfirmationResponse();
        resp.setBookingReference(booking.getBookingReference());
        resp.setClientName(booking.getClient() != null ? booking.getClient().getFullName() : "Client");
        resp.setEventDate(booking.getEventDate());
        resp.setEstimatedAmount(estimated);

        User recordedByUser = modifier != null ? modifier : booking.getCreatedBy();
        if (recordedByUser == null) {
            throw new IllegalArgumentException("Authentication required to confirm booking. A user must record the payment.");
        }

        if (request.isClientPaidFull()) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaidAmount(estimated);
            bookingRepository.save(booking);
            Payment payment = Payment.builder()
                    .branch(booking.getBranch())
                    .booking(booking)
                    .client(booking.getClient())
                    .type(Payment.PaymentType.INCOME)
                    .amount(estimated)
                    .remainingBalance(BigDecimal.ZERO)
                    .paymentMethod(method)
                    .paymentStatus(PaymentStatus.PAID)
                    .description(notes)
                    .recordedBy(recordedByUser)
                    .build();
            paymentRepository.save(payment);
            notificationService.notifyBookingConfirmed(booking, true, estimated, BigDecimal.ZERO);
            resp.setConfirmed(true);
            resp.setPaidAmount(estimated);
            resp.setRemainingBalance(BigDecimal.ZERO);
            resp.setPaymentStatus(PaymentStatus.PAID);
            resp.setMessage("Booking confirmed. Client has paid the full amount.");
            resp.setMessageFr("Réservation confirmée. Le client a payé le montant total.");
            return resp;
        }

        if (request.isForceConfirm()) {
            BigDecimal paid = request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO;
            if (paid.compareTo(BigDecimal.ZERO) < 0) paid = BigDecimal.ZERO;
            BigDecimal remaining = estimated.subtract(paid);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaidAmount(paid);
            bookingRepository.save(booking);
            Payment payment = Payment.builder()
                    .branch(booking.getBranch())
                    .booking(booking)
                    .client(booking.getClient())
                    .type(Payment.PaymentType.INCOME)
                    .amount(paid)
                    .remainingBalance(remaining)
                    .paymentMethod(method)
                    .paymentStatus(PaymentStatus.PARTIAL)
                    .description(notes)
                    .recordedBy(recordedByUser)
                    .build();
            paymentRepository.save(payment);
            notificationService.notifyBookingConfirmed(booking, false, paid, remaining);
            resp.setConfirmed(true);
            resp.setPaidAmount(paid);
            resp.setRemainingBalance(remaining);
            resp.setPaymentStatus(PaymentStatus.PARTIAL);
            resp.setMessage("Booking confirmed. Partial payment recorded. Remaining balance: " + remaining + " RWF");
            resp.setMessageFr("Réservation confirmée. Paiement partiel enregistré. Solde restant: " + remaining + " RWF");
            return resp;
        }

        resp.setConfirmed(false);
        resp.setEstimatedAmount(estimated);
        resp.setMessage("Booking was not confirmed. Please indicate payment details to proceed.");
        resp.setMessageFr("La réservation n'a pas été confirmée. Veuillez indiquer les détails de paiement pour continuer.");
        return resp;
    }

    private String buildPackagesText(Booking b) {
        if (b.getBookingPackages() == null || b.getBookingPackages().isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (BookingPackage bp : b.getBookingPackages()) {
            String name = bp.getPackageItem() != null ? bp.getPackageItem().getName() : "Package";
            BigDecimal price = bp.getTotalPrice() != null ? bp.getTotalPrice() : (bp.getUnitPrice() != null ? bp.getUnitPrice() : BigDecimal.ZERO);
            sb.append("- ").append(name).append(": ").append(price).append(" RWF\n");
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public PaymentSummaryDto getPaymentSummary(Long bookingId) {
        Booking booking = getById(bookingId);
        BigDecimal estimated = booking.getEstimatedAmount() != null ? booking.getEstimatedAmount() : BigDecimal.ZERO;
        List<Payment> payments = paymentRepository.findByBooking_IdOrderByRecordedAtDesc(bookingId);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getType() == Payment.PaymentType.INCOME)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = estimated.subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
        PaymentStatus pStatus = remaining.compareTo(BigDecimal.ZERO) == 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL;

        PaymentSummaryDto dto = new PaymentSummaryDto();
        dto.setEstimatedAmount(estimated);
        dto.setTotalPaidAmount(totalPaid);
        dto.setRemainingBalance(remaining);
        dto.setPaymentStatus(pStatus);
        List<PaymentSummaryDto.PaymentItemDto> items = payments.stream()
                .filter(p -> p.getType() == Payment.PaymentType.INCOME)
                .map(p -> {
                    PaymentSummaryDto.PaymentItemDto item = new PaymentSummaryDto.PaymentItemDto();
                    item.setId(p.getId());
                    item.setRecordedAt(p.getRecordedAt());
                    item.setAmount(p.getAmount());
                    item.setPaymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null);
                    item.setNotes(p.getDescription());
                    return item;
                })
                .collect(Collectors.toList());
        dto.setPayments(items);
        return dto;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getBookingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", bookingRepository.count());
        stats.put("pendingCount", bookingRepository.countByStatus(BookingStatus.PENDING));
        stats.put("confirmedCount", bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        stats.put("completedCount", bookingRepository.countByStatus(BookingStatus.COMPLETED));
        stats.put("cancelledCount", bookingRepository.countByStatus(BookingStatus.CANCELLED));
        stats.put("inProgressCount", bookingRepository.countByStatus(BookingStatus.IN_PROGRESS));
        BigDecimal revenue = bookingRepository.sumEstimatedAmountByConfirmedAndCompleted();
        stats.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        return stats;
    }
}
