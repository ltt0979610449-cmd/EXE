package swd.coiviet.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd.coiviet.model.EmailLog;
import swd.coiviet.repository.EmailLogRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Endpoint tracking mở email - trả về 1x1 pixel transparent GIF.
 * Được nhúng trong HTML email để ghi nhận khi khách mở email.
 */
@RestController
@RequestMapping("/api/track")
@Tag(name = "Track", description = "Tracking mở email (pixel)")
public class EmailTrackingController {

    private static final byte[] TRANSPARENT_1X1_GIF = new byte[]{
            0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00, (byte) 0x80, 0x00, 0x00,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x00, 0x21, (byte) 0xf9, 0x04, 0x01,
            0x00, 0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            0x02, 0x02, 0x44, 0x01, 0x00, 0x3b
    };

    private final EmailLogRepository emailLogRepository;

    public EmailTrackingController(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
    }

    @GetMapping("/email/{id}/open")
    public ResponseEntity<byte[]> trackEmailOpen(@PathVariable Long id) {
        Optional<EmailLog> opt = emailLogRepository.findById(id);
        if (opt.isPresent()) {
            EmailLog log = opt.get();
            if (log.getOpenedAt() == null) {
                log.setOpenedAt(LocalDateTime.now());
            }
            log.setOpenedCount((log.getOpenedCount() != null ? log.getOpenedCount() : 0) + 1);
            emailLogRepository.save(log);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_GIF);
        headers.setCacheControl("no-store, no-cache, must-revalidate");
        return new ResponseEntity<>(TRANSPARENT_1X1_GIF, headers, HttpStatus.OK);
    }
}
