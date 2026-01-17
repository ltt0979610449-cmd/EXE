package swd.coiviet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.dto.request.CancelBookingRequest;
import swd.coiviet.dto.request.CreateBookingRequest;
import swd.coiviet.dto.response.BookingResponse;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.enums.PaymentStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Booking;
import swd.coiviet.model.Payment;
import swd.coiviet.model.Tour;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.model.User;
import swd.coiviet.model.Voucher;
import swd.coiviet.repository.BookingRepository;
import swd.coiviet.service.BookingService;
import swd.coiviet.service.PaymentService;
import swd.coiviet.service.TourScheduleService;
import swd.coiviet.service.TourService;
import swd.coiviet.service.UserService;
import swd.coiviet.service.VoucherService;
import swd.coiviet.service.TourWorkflowService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepo;
    private final TourService tourService;
    private final TourScheduleService tourScheduleService;
    private final UserService userService;
    private final VoucherService voucherService;
    private final PaymentService paymentService;
    private final TourWorkflowService tourWorkflowService;
    private final swd.coiviet.service.EmailService emailService;

    public BookingServiceImpl(
            BookingRepository bookingRepo,
            TourService tourService,
            TourScheduleService tourScheduleService,
            UserService userService,
            VoucherService voucherService,
            PaymentService paymentService,
            TourWorkflowService tourWorkflowService,
            swd.coiviet.service.EmailService emailService) {
        this.bookingRepo = bookingRepo;
        this.tourService = tourService;
        this.tourScheduleService = tourScheduleService;
        this.userService = userService;
        this.voucherService = voucherService;
        this.paymentService = paymentService;
        this.tourWorkflowService = tourWorkflowService;
        this.emailService = emailService;
    }

    @Override
    public Booking save(Booking b) {
        return bookingRepo.save(b);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingRepo.findById(id);
    }

    @Override
    public Optional<Booking> findByBookingCode(String code) {
        return bookingRepo.findByBookingCode(code);
    }

    @Override
    public List<Booking> findByUserId(Long userId) {
        return bookingRepo.findByUserId(userId);
    }

    @Override
    public List<Booking> findByTourScheduleId(Long tourScheduleId) {
        return bookingRepo.findByTourScheduleId(tourScheduleId);
    }

    @Override
    public void deleteById(Long id) {
        bookingRepo.deleteById(id);
    }

    @Override
    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {
        // Validate user
        User user = userService.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Người dùng không tồn tại"));

        // Validate tour
        Tour tour = tourService.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));

        // Validate tour schedule
        TourSchedule schedule = tourScheduleService.findById(request.getTourScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));

        // Check if schedule belongs to tour
        if (!schedule.getTour().getId().equals(tour.getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Lịch trình không thuộc tour này");
        }

        // Check availability
        if (!tourWorkflowService.checkTourAvailability(schedule.getId(), request.getNumParticipants())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tour không còn đủ chỗ");
        }

        // Calculate price
        BigDecimal basePrice = schedule.getCurrentPrice() != null 
                ? schedule.getCurrentPrice() 
                : tour.getPrice();
        BigDecimal totalAmount = basePrice.multiply(BigDecimal.valueOf(request.getNumParticipants()));
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount;

        // Apply voucher if provided
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            Optional<Voucher> voucherOpt = voucherService.findByCode(request.getVoucherCode());
            if (voucherOpt.isPresent()) {
                Voucher voucher = voucherOpt.get();
                LocalDateTime now = LocalDateTime.now();
                
                if (voucher.getIsActive() 
                        && voucher.getValidFrom() != null && now.isAfter(voucher.getValidFrom())
                        && voucher.getValidUntil() != null && now.isBefore(voucher.getValidUntil())
                        && (voucher.getMaxUsage() == null || voucher.getCurrentUsage() < voucher.getMaxUsage())
                        && (voucher.getMinPurchase() == null || finalAmount.compareTo(voucher.getMinPurchase()) >= 0)) {
                    
                    if ("PERCENTAGE".equals(voucher.getDiscountType())) {
                        discountAmount = finalAmount.multiply(voucher.getDiscountValue())
                                .divide(BigDecimal.valueOf(100));
                    } else if ("FIXED".equals(voucher.getDiscountType())) {
                        discountAmount = voucher.getDiscountValue();
                    }
                    
                    if (discountAmount.compareTo(finalAmount) > 0) {
                        discountAmount = finalAmount;
                    }
                    
                    finalAmount = finalAmount.subtract(discountAmount);
                    
                    // Update voucher usage
                    voucher.setCurrentUsage(voucher.getCurrentUsage() + 1);
                    voucherService.save(voucher);
                }
            }
        }

        // Create booking
        String bookingCode = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .tour(tour)
                .tourSchedule(schedule)
                .numParticipants(request.getNumParticipants())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentMethod(request.getPaymentMethod())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        booking = bookingRepo.save(booking);

        // Update tour schedule booked slots
        schedule.setBookedSlots(schedule.getBookedSlots() + request.getNumParticipants());
        if (schedule.getBookedSlots() >= schedule.getMaxSlots()) {
            schedule.setStatus(swd.coiviet.enums.TourScheduleStatus.FULL);
        }
        tourScheduleService.save(schedule);

        // Create payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .transactionId("TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paymentMethod(request.getPaymentMethod())
                .amount(finalAmount)
                .status(PaymentStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .build();
        paymentService.save(payment);

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long userId, Long bookingId, CancelBookingRequest request) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking không tồn tại"));

        // Check ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền hủy booking này");
        }

        // Check if already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Booking đã được hủy");
        }

        // Check if already completed
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể hủy booking đã hoàn thành");
        }

        // Calculate cancellation fee
        BigDecimal cancellationFee = calculateCancellationFee(booking);
        BigDecimal refundAmount = booking.getFinalAmount().subtract(cancellationFee);

        // Update booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationFee(cancellationFee);
        booking.setRefundAmount(refundAmount);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = bookingRepo.save(booking);

        // Update payment status
        List<Payment> payments = paymentService.findByBookingId(bookingId);
        for (Payment payment : payments) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                payment.setRefundReason(request.getReason() != null ? request.getReason() : "Khách hàng hủy tour");
                paymentService.save(payment);
            }
        }

        // Update tour schedule booked slots
        TourSchedule schedule = booking.getTourSchedule();
        schedule.setBookedSlots(Math.max(0, schedule.getBookedSlots() - booking.getNumParticipants()));
        if (schedule.getStatus() == swd.coiviet.enums.TourScheduleStatus.FULL) {
            schedule.setStatus(swd.coiviet.enums.TourScheduleStatus.SCHEDULED);
        }
        tourScheduleService.save(schedule);

        return mapToResponse(booking);
    }

    @Override
    public BigDecimal calculateCancellationFee(Booking booking) {
        if (booking.getTourSchedule() == null || booking.getTourSchedule().getTourDate() == null) {
            return booking.getFinalAmount(); // 100% fee if no date
        }

        LocalDate tourDate = booking.getTourSchedule().getTourDate();
        LocalDate now = LocalDate.now();
        long daysUntilTour = java.time.temporal.ChronoUnit.DAYS.between(now, tourDate);

        BigDecimal feePercent;
        if (daysUntilTour > 10) {
            feePercent = BigDecimal.valueOf(15); // 10-20%
        } else if (daysUntilTour >= 6) {
            feePercent = BigDecimal.valueOf(40); // 30-50%
        } else if (daysUntilTour >= 3) {
            feePercent = BigDecimal.valueOf(75); // 70-80%
        } else {
            feePercent = BigDecimal.valueOf(100); // 100%
        }

        return booking.getFinalAmount().multiply(feePercent).divide(BigDecimal.valueOf(100));
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUser().getId())
                .tourId(booking.getTour().getId())
                .tourTitle(booking.getTour().getTitle())
                .tourScheduleId(booking.getTourSchedule().getId())
                .tourDate(booking.getTourSchedule().getTourDate() != null 
                        ? booking.getTourSchedule().getTourDate().atStartOfDay() 
                        : null)
                .tourStartTime(booking.getTourSchedule().getStartTime() != null 
                        ? booking.getTourSchedule().getTourDate().atTime(booking.getTourSchedule().getStartTime())
                        : null)
                .numParticipants(booking.getNumParticipants())
                .contactName(booking.getContactName())
                .contactPhone(booking.getContactPhone())
                .contactEmail(booking.getContactEmail())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .paidAt(booking.getPaidAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationFee(booking.getCancellationFee())
                .refundAmount(booking.getRefundAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
