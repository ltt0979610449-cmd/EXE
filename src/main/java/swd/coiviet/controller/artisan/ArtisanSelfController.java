package swd.coiviet.controller.artisan;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd.coiviet.configuration.JwtUtil;
import swd.coiviet.dto.request.CancelBookingRequest;
import swd.coiviet.dto.response.ApiResponse;
import swd.coiviet.dto.response.ArtisanFollowersResponse;
import swd.coiviet.dto.response.BookingResponse;
import swd.coiviet.enums.BookingStatus;
import swd.coiviet.enums.TourScheduleStatus;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.model.Artisan;
import swd.coiviet.model.Booking;
import swd.coiviet.model.Province;
import swd.coiviet.model.Tour;
import swd.coiviet.model.TourSchedule;
import swd.coiviet.repository.UserFollowArtisanRepository;
import swd.coiviet.service.ArtisanService;
import swd.coiviet.service.BookingService;
import swd.coiviet.service.CloudinaryService;
import swd.coiviet.service.ProvinceService;
import swd.coiviet.service.TourScheduleService;
import swd.coiviet.service.TourService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/artisans/me")
@Tag(name = "Artisan", description = "Tự quản lý profile nghệ nhân")
public class ArtisanSelfController {

    private final ArtisanService artisanService;
    private final BookingService bookingService;
    private final TourScheduleService tourScheduleService;
    private final TourService tourService;
    private final UserFollowArtisanRepository userFollowArtisanRepository;
    private final JwtUtil jwtUtil;
    private final CloudinaryService cloudinaryService;
    private final ProvinceService provinceService;

    public ArtisanSelfController(ArtisanService artisanService,
                                BookingService bookingService,
                                TourScheduleService tourScheduleService,
                                TourService tourService,
                                UserFollowArtisanRepository userFollowArtisanRepository,
                                JwtUtil jwtUtil,
                                CloudinaryService cloudinaryService,
                                ProvinceService provinceService) {
        this.artisanService = artisanService;
        this.bookingService = bookingService;
        this.tourScheduleService = tourScheduleService;
        this.tourService = tourService;
        this.userFollowArtisanRepository = userFollowArtisanRepository;
        this.jwtUtil = jwtUtil;
        this.cloudinaryService = cloudinaryService;
        this.provinceService = provinceService;
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn");
            }
            Claims claims = jwtUtil.getClaims(token);
            Integer userId = claims.get("userId", Integer.class);
            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token không chứa thông tin user");
            }
            return Long.valueOf(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token không hợp lệ: " + e.getMessage());
        }
    }

    private Artisan getCurrentArtisan(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return artisanService.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bạn chưa có hồ sơ nghệ nhân"));
    }

    // ==================== BOOKINGS ====================

    @GetMapping("/bookings")
    @Operation(summary = "Danh sách bookings của nghệ nhân")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        List<Booking> bookings = bookingService.findByArtisanIdWithFilters(artisan.getId(), status, from, to);
        List<BookingResponse> responses = bookings.stream()
                .map(bookingService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/bookings/{id}")
    @Operation(summary = "Chi tiết booking")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        Booking booking = bookingService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking không tồn tại"));
        if (booking.getTour() == null || booking.getTour().getArtisan() == null
                || !booking.getTour().getArtisan().getId().equals(artisan.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem booking này");
        }
        return ResponseEntity.ok(ApiResponse.success(bookingService.toResponse(booking)));
    }

    @PutMapping("/bookings/{id}/confirm")
    @Operation(summary = "Xác nhận booking")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable Long id,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        BookingResponse response = bookingService.confirmBookingByArtisan(artisan.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Xác nhận booking thành công"));
    }

    @PutMapping("/bookings/{id}/cancel")
    @Operation(summary = "Hủy booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest cancelRequest,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        if (cancelRequest == null) {
            cancelRequest = new CancelBookingRequest();
        }
        BookingResponse response = bookingService.cancelBookingByArtisan(artisan.getId(), id, cancelRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Hủy booking thành công"));
    }

    // ==================== PROFILE ====================

    @PutMapping(value = "", consumes = {"multipart/form-data"})
    @Operation(summary = "Cập nhật hồ sơ nghệ nhân", description = "Nghệ nhân cập nhật hồ sơ của chính mình (dựa vào token, không cần truyền ID)")
    public ResponseEntity<ApiResponse<Artisan>> updateMyProfile(
            HttpServletRequest request,
            @Parameter(description = "Họ tên", required = false)
            @RequestParam(required = false) String fullName,
            @Parameter(description = "Chuyên môn", required = false)
            @RequestParam(required = false) String specialization,
            @Parameter(description = "Tiểu sử", required = false)
            @RequestParam(required = false) String bio,
            @Parameter(description = "ID tỉnh thành", required = false)
            @RequestParam(required = false) Long provinceId,
            @Parameter(description = "Địa chỉ xưởng", required = false)
            @RequestParam(required = false) String workshopAddress,
            @Parameter(description = "Dân tộc (vd: Mường, Jrai)", required = false)
            @RequestParam(required = false) String ethnicity,
            @Parameter(description = "Ngày sinh (yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String dateOfBirth,
            @Parameter(description = "Mô tả ngắn cho hero", required = false)
            @RequestParam(required = false) String heroSubtitle,
            @Parameter(description = "Narrative JSON: [{\"title\":\"...\",\"content\":\"...\",\"imageUrl\":\"...\"}]", required = false)
            @RequestParam(required = false) String narrativeContent,
            @Parameter(description = "Ảnh profile mới (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @Parameter(description = "Ảnh panorama full-width (nếu có)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "panoramaImage", required = false) MultipartFile panoramaImage,
            @Parameter(description = "Danh sách ảnh mới (nếu có, có thể chọn nhiều ảnh)", 
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        Artisan existing = getCurrentArtisan(request);
        Long id = existing.getId();
        try {
            if (fullName != null) existing.setFullName(fullName);
            if (specialization != null) existing.setSpecialization(specialization);
            if (bio != null) existing.setBio(bio);
            if (workshopAddress != null) existing.setWorkshopAddress(workshopAddress);
            if (ethnicity != null) existing.setEthnicity(ethnicity);
            if (heroSubtitle != null) existing.setHeroSubtitle(heroSubtitle);
            if (narrativeContent != null) existing.setNarrativeContent(narrativeContent);
            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                try {
                    existing.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (Exception ignored) { }
            }
            if (provinceId != null) {
                Province province = provinceService.findById(provinceId)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tỉnh thành không tồn tại"));
                existing.setProvince(province);
            }
            if (profileImage != null && !profileImage.isEmpty() && profileImage.getSize() > 0) {
                if (existing.getProfileImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getProfileImageUrl());
                    if (publicId != null) cloudinaryService.deleteResource(publicId);
                }
                String profileImageUrl = cloudinaryService.uploadArtisanProfileImage(profileImage, id);
                existing.setProfileImageUrl(profileImageUrl);
            }
            if (panoramaImage != null && !panoramaImage.isEmpty() && panoramaImage.getSize() > 0) {
                if (existing.getPanoramaImageUrl() != null) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existing.getPanoramaImageUrl());
                    if (publicId != null) cloudinaryService.deleteResource(publicId);
                }
                String panoramaUrl = cloudinaryService.uploadArtisanPanoramaImage(panoramaImage, id);
                existing.setPanoramaImageUrl(panoramaUrl);
            }
            if (images != null && images.length > 0) {
                java.util.List<MultipartFile> validImages = Arrays.stream(images)
                        .filter(img -> img != null && !img.isEmpty() && img.getSize() > 0)
                        .collect(java.util.stream.Collectors.toList());
                if (!validImages.isEmpty()) {
                    if (existing.getImages() != null && !existing.getImages().isEmpty()) {
                        String[] oldImageUrls = existing.getImages().split(",");
                        for (String oldUrl : oldImageUrls) {
                            String publicId = cloudinaryService.extractPublicIdFromUrl(oldUrl.trim());
                            if (publicId != null) cloudinaryService.deleteResource(publicId);
                        }
                    }
                    MultipartFile[] validImagesArray = validImages.toArray(new MultipartFile[0]);
                    java.util.List<String> imageUrls = cloudinaryService.uploadArtisanImages(validImagesArray, id);
                    String imagesJson = String.join(",", imageUrls);
                    existing.setImages(imagesJson);
                }
            }
            Artisan updated = artisanService.save(existing);
            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật hồ sơ thành công"));
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    // ==================== FOLLOWERS ====================

    @GetMapping("/followers")
    @Operation(summary = "Số người theo dõi")
    public ResponseEntity<ApiResponse<ArtisanFollowersResponse>> getFollowers(HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        long count = userFollowArtisanRepository.countByArtisanId(artisan.getId());
        ArtisanFollowersResponse response = ArtisanFollowersResponse.builder().count(count).build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== TOUR SCHEDULES ====================

    @GetMapping("/tours/{tourId}/schedules")
    @Operation(summary = "Danh sách lịch của tour")
    public ResponseEntity<ApiResponse<List<TourSchedule>>> getSchedules(
            @PathVariable Long tourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        Tour tour = tourService.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        if (tour.getArtisan() == null || !tour.getArtisan().getId().equals(artisan.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tour không thuộc về bạn");
        }
        List<TourSchedule> schedules;
        if (date != null) {
            schedules = tourScheduleService.findByTourIdAndDate(tourId, date);
        } else {
            schedules = tourScheduleService.findByTourId(tourId);
        }
        return ResponseEntity.ok(ApiResponse.success(schedules));
    }

    @PostMapping("/tours/{tourId}/schedules")
    @Operation(summary = "Tạo lịch mới")
    public ResponseEntity<ApiResponse<TourSchedule>> createSchedule(
            @PathVariable Long tourId,
            @RequestParam @NotNull(message = "Ngày tour không được để trống")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tourDate,
            @RequestParam @NotNull(message = "Giờ bắt đầu không được để trống")
            @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam @NotNull(message = "Số chỗ tối đa không được để trống")
            @Min(value = 1, message = "Số chỗ tối đa phải lớn hơn 0") Integer maxSlots,
            @RequestParam(required = false) BigDecimal currentPrice,
            @RequestParam(required = false) Integer discountPercent,
            @RequestParam(required = false) TourScheduleStatus status,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        Tour tour = tourService.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Tour không tồn tại"));
        if (tour.getArtisan() == null || !tour.getArtisan().getId().equals(artisan.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tour không thuộc về bạn");
        }
        tourScheduleService.findByTourIdAndTourDateAndStartTime(tourId, tourDate, startTime).ifPresent(s -> {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đã tồn tại lịch trình cho tour này vào ngày và giờ đã chọn");
        });
        TourSchedule schedule = TourSchedule.builder()
                .tour(tour)
                .tourDate(tourDate)
                .startTime(startTime)
                .maxSlots(maxSlots)
                .bookedSlots(0)
                .currentPrice(currentPrice)
                .discountPercent(discountPercent)
                .status(status != null ? status : TourScheduleStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();
        TourSchedule saved = tourScheduleService.save(schedule);
        return ResponseEntity.ok(ApiResponse.success(saved, "Tạo lịch trình tour thành công"));
    }

    @GetMapping("/schedules/{id}")
    @Operation(summary = "Chi tiết lịch")
    public ResponseEntity<ApiResponse<TourSchedule>> getScheduleDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        TourSchedule schedule = tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));
        if (schedule.getTour() == null || schedule.getTour().getArtisan() == null
                || !schedule.getTour().getArtisan().getId().equals(artisan.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Lịch trình không thuộc về bạn");
        }
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @PutMapping("/schedules/{id}")
    @Operation(summary = "Cập nhật lịch")
    public ResponseEntity<ApiResponse<TourSchedule>> updateSchedule(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tourDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam(required = false) @Min(value = 1) Integer maxSlots,
            @RequestParam(required = false) BigDecimal currentPrice,
            @RequestParam(required = false) Integer discountPercent,
            @RequestParam(required = false) TourScheduleStatus status,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        TourSchedule existing = tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));
        if (existing.getTour() == null || existing.getTour().getArtisan() == null
                || !existing.getTour().getArtisan().getId().equals(artisan.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Lịch trình không thuộc về bạn");
        }
        if (tourDate != null) existing.setTourDate(tourDate);
        if (startTime != null) existing.setStartTime(startTime);
        if (maxSlots != null) {
            Integer bookedSlots = existing.getBookedSlots() != null ? existing.getBookedSlots() : 0;
            if (maxSlots < bookedSlots) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Max slots không thể nhỏ hơn số chỗ đã đặt");
            }
            existing.setMaxSlots(maxSlots);
        }
        if (currentPrice != null) existing.setCurrentPrice(currentPrice);
        if (discountPercent != null) existing.setDiscountPercent(discountPercent);
        if (status != null) existing.setStatus(status);
        TourSchedule updated = tourScheduleService.save(existing);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật lịch trình tour thành công"));
    }

    @DeleteMapping("/schedules/{id}")
    @Operation(summary = "Xóa lịch")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable Long id,
            HttpServletRequest request) {
        Artisan artisan = getCurrentArtisan(request);
        TourSchedule schedule = tourScheduleService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lịch trình tour không tồn tại"));
        if (schedule.getTour() == null || schedule.getTour().getArtisan() == null
                || !schedule.getTour().getArtisan().getId().equals(artisan.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Lịch trình không thuộc về bạn");
        }
        List<Booking> bookings = bookingService.findByTourScheduleId(id);
        boolean hasActiveBooking = bookings.stream()
                .anyMatch(b -> b.getStatus() != BookingStatus.CANCELLED);
        if (hasActiveBooking) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Không thể xóa lịch trình tour đang có booking chưa hủy");
        }
        tourScheduleService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa lịch trình tour thành công"));
    }
}
