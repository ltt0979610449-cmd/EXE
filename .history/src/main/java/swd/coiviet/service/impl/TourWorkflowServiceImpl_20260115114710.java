package swd.coiviet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.dto.request.SuggestTourRequest;
import swd.coiviet.dto.response.TourSuggestionResponse;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.model.Tour;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.model.Voucher;
import swd.coiviet.repository.BookingRepository;
import swd.coiviet.repository.TourScheduleRepository;
import swd.coiviet.service.TourService;
import swd.coiviet.service.TourScheduleService;
import swd.coiviet.service.TourWorkflowService;
import swd.coiviet.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourWorkflowServiceImpl implements TourWorkflowService {
    private static final Logger logger = LoggerFactory.getLogger(TourWorkflowServiceImpl.class);
    
    private final TourScheduleRepository tourScheduleRepo;
    private final BookingRepository bookingRepo;
    private final TourService tourService;
    private final TourScheduleService tourScheduleService;
    private final VoucherService voucherService;
    private final swd.coiviet.service.EmailService emailService;

    public TourWorkflowServiceImpl(
            TourScheduleRepository tourScheduleRepo,
            BookingRepository bookingRepo,
            TourService tourService,
            TourScheduleService tourScheduleService,
            VoucherService voucherService,
            swd.coiviet.service.EmailService emailService) {
        this.tourScheduleRepo = tourScheduleRepo;
        this.bookingRepo = bookingRepo;
        this.tourService = tourService;
        this.tourScheduleService = tourScheduleService;
        this.voucherService = voucherService;
        this.emailService = emailService;
    }

    @Override
    public boolean checkTourAvailability(Long tourScheduleId, Integer numParticipants) {
        Optional<TourSchedule> scheduleOpt = tourScheduleService.findById(tourScheduleId);
        if (scheduleOpt.isEmpty()) {
            return false;
        }

        TourSchedule schedule = scheduleOpt.get();
        
        // Check if schedule is available
        if (schedule.getStatus() != TourScheduleStatus.SCHEDULED) {
            return false;
        }

        // Check if tour date is in the past
        if (schedule.getTourDate() != null && schedule.getTourDate().isBefore(LocalDate.now())) {
            return false;
        }

        // Check available slots
        int bookedSlots = schedule.getBookedSlots() != null ? schedule.getBookedSlots() : 0;
        int maxSlots = schedule.getMaxSlots() != null ? schedule.getMaxSlots() : 0;
        int availableSlots = maxSlots - bookedSlots;

        return availableSlots >= numParticipants;
    }

    @Override
    @Transactional
    public void handleLowBookingTour(TourSchedule schedule) {
        if (schedule.getTourDate() == null) {
            return;
        }

        LocalDate tourDate = schedule.getTourDate();
        LocalDate now = LocalDate.now();
        long daysUntilTour = java.time.temporal.ChronoUnit.DAYS.between(now, tourDate);

        // Count confirmed bookings
        List<swd.coiviet.model.Booking> bookings = bookingRepo.findByTourScheduleIdAndStatus(
                schedule.getId(), BookingStatus.CONFIRMED);

        int bookedSlots = schedule.getBookedSlots() != null ? schedule.getBookedSlots() : 0;
        int maxSlots = schedule.getMaxSlots() != null ? schedule.getMaxSlots() : 0;
        int minRequired = (int) (maxSlots * 0.5); // Minimum 50% capacity

        // Only process if below minimum requirement
        if (bookedSlots >= minRequired) {
            return;
        }

        if (daysUntilTour > 7) {
            // Còn >7 ngày: Đẩy ads, telesale, khuyến mãi
            logger.info("Tour schedule {} còn {} ngày, số lượng booking thấp. Đẩy ads và khuyến mãi", 
                    schedule.getId(), daysUntilTour);
            
            // Create discount voucher
            Voucher voucher = createDiscountVoucherForSchedule(schedule, 20); // 20% discount
            
            // Send email to existing bookings with discount
            for (swd.coiviet.model.Booking booking : bookings) {
                if (booking.getContactEmail() != null) {
                    emailService.sendTourLowBookingAlert(
                            booking.getContactEmail(),
                            schedule.getTour().getTitle(),
                            schedule.getTourDate().toString(),
                            voucher.getCode(),
                            20
                    );
                }
            }
            
        } else if (daysUntilTour >= 3 && daysUntilTour <= 5) {
            // Còn 3-5 ngày: Gửi email cho khách, đề xuất tour dự phòng
            logger.info("Tour schedule {} còn {} ngày, số lượng booking thấp. Gửi email cho khách", 
                    schedule.getId(), daysUntilTour);
            
            // Create higher discount voucher
            createDiscountVoucherForSchedule(schedule, 30); // 30% discount
            
            // Send email to existing bookings
            for (swd.coiviet.model.Booking booking : bookings) {
                // TODO: Send email with alternative tour suggestions
                logger.info("Gửi email cho booking {} với đề xuất tour dự phòng", booking.getBookingCode());
            }
            
        } else if (daysUntilTour < 3) {
            // Còn <3 ngày: Quyết định hủy hoặc phụ thu
            logger.warn("Tour schedule {} còn {} ngày, số lượng booking thấp. Quyết định hủy hoặc phụ thu", 
                    schedule.getId(), daysUntilTour);
            
            if (bookedSlots < maxSlots * 0.3) { // Less than 30% capacity
                // Cancel the tour
                schedule.setStatus(TourScheduleStatus.CANCELLED);
                tourScheduleService.save(schedule);
                
                // Notify and refund bookings
                for (swd.coiviet.model.Booking booking : bookings) {
                    // TODO: Send cancellation email and process refund
                    logger.info("Hủy booking {} do tour bị hủy", booking.getBookingCode());
                }
            } else {
                // Apply surcharge
                BigDecimal surchargePercent = BigDecimal.valueOf(20); // 20% surcharge
                BigDecimal newPrice = schedule.getCurrentPrice() != null 
                        ? schedule.getCurrentPrice() 
                        : schedule.getTour().getPrice();
                newPrice = newPrice.multiply(BigDecimal.ONE.add(surchargePercent.divide(BigDecimal.valueOf(100))));
                schedule.setCurrentPrice(newPrice);
                tourScheduleService.save(schedule);
                
                // Notify bookings about surcharge
                for (swd.coiviet.model.Booking booking : bookings) {
                    // TODO: Send email about surcharge
                    logger.info("Thông báo phụ thu cho booking {}", booking.getBookingCode());
                }
            }
        }
    }

    @Override
    public List<TourSuggestionResponse> suggestTours(SuggestTourRequest request) {
        List<TourSuggestionResponse> suggestions = new ArrayList<>();
        
        // Get tours by province
        List<Tour> tours = tourService.findByProvinceId(request.getProvinceId());
        
        LocalDate preferredDate = request.getPreferredDate() != null 
                ? request.getPreferredDate() 
                : LocalDate.now();
        
        for (Tour tour : tours) {
            // Find next available schedule
            Optional<TourSchedule> scheduleOpt = tourScheduleRepo.findFirstAvailableByTourId(
                    tour.getId(), 
                    preferredDate, 
                    TourScheduleStatus.SCHEDULED);
            
            if (scheduleOpt.isEmpty()) {
                continue;
            }
            
            TourSchedule schedule = scheduleOpt.get();
            int availableSlots = schedule.getMaxSlots() - (schedule.getBookedSlots() != null ? schedule.getBookedSlots() : 0);
            
            // Check if meets participant requirement
            if (request.getNumParticipants() != null && availableSlots < request.getNumParticipants()) {
                continue;
            }
            
            TourSuggestionResponse suggestion = TourSuggestionResponse.builder()
                    .tourId(tour.getId())
                    .tourTitle(tour.getTitle())
                    .tourDescription(tour.getDescription())
                    .thumbnailUrl(tour.getThumbnailUrl())
                    .provinceId(tour.getProvince().getId())
                    .provinceName(tour.getProvince().getName())
                    .artisanId(tour.getArtisan() != null ? tour.getArtisan().getId() : null)
                    .artisanName(tour.getArtisan() != null ? tour.getArtisan().getFullName() : null)
                    .artisanSpecialization(tour.getArtisan() != null ? tour.getArtisan().getSpecialization() : null)
                    .price(schedule.getCurrentPrice() != null ? schedule.getCurrentPrice() : tour.getPrice())
                    .durationHours(tour.getDurationHours())
                    .maxParticipants(tour.getMaxParticipants())
                    .availableSlots(availableSlots)
                    .nextAvailableDate(schedule.getTourDate())
                    .nextAvailableTime(schedule.getStartTime())
                    .averageRating(tour.getAverageRating())
                    .totalBookings(tour.getTotalBookings())
                    .reason("Tour phù hợp với vị trí bạn chọn và có lịch trình sẵn có")
                    .build();
            
            suggestions.add(suggestion);
        }
        
        // Sort by rating and availability
        suggestions.sort((a, b) -> {
            int ratingCompare = (b.getAverageRating() != null ? b.getAverageRating() : BigDecimal.ZERO)
                    .compareTo(a.getAverageRating() != null ? a.getAverageRating() : BigDecimal.ZERO);
            if (ratingCompare != 0) return ratingCompare;
            
            int availabilityCompare = Integer.compare(b.getAvailableSlots(), a.getAvailableSlots());
            if (availabilityCompare != 0) return availabilityCompare;
            
            return a.getNextAvailableDate().compareTo(b.getNextAvailableDate());
        });
        
        return suggestions.stream().limit(10).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processUpcomingTours() {
        logger.info("Bắt đầu xử lý các tour sắp tới");
        
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusDays(10);
        
        List<TourSchedule> upcomingSchedules = tourScheduleRepo.findUpcomingSchedules(
                fromDate, toDate, TourScheduleStatus.SCHEDULED);
        
        for (TourSchedule schedule : upcomingSchedules) {
            try {
                handleLowBookingTour(schedule);
            } catch (Exception e) {
                logger.error("Lỗi khi xử lý tour schedule {}: {}", schedule.getId(), e.getMessage(), e);
            }
        }
        
        logger.info("Hoàn thành xử lý {} tour schedules", upcomingSchedules.size());
    }

    @Override
    @Transactional
    public void createDiscountVoucherForSchedule(TourSchedule schedule, Integer discountPercent) {
        String voucherCode = "TOUR" + schedule.getId() + "-" + 
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        BigDecimal minPurchase = schedule.getCurrentPrice() != null 
                ? schedule.getCurrentPrice() 
                : schedule.getTour().getPrice();
        
        Voucher voucher = Voucher.builder()
                .code(voucherCode)
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(discountPercent))
                .minPurchase(minPurchase)
                .maxUsage(100)
                .currentUsage(0)
                .validFrom(LocalDateTime.now())
                .validUntil(schedule.getTourDate().atTime(LocalTime.MAX))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        voucherService.save(voucher);
        
        logger.info("Đã tạo voucher {} giảm {}% cho tour schedule {}", 
                voucherCode, discountPercent, schedule.getId());
    }
}
