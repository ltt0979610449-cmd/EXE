package swd.coiviet.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class LocalTimeConverter implements Converter<String, LocalTime> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalTime convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        source = source.trim();

        // Try to parse as HH:mm format first
        try {
            return LocalTime.parse(source, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // If that fails, try to parse as ISO datetime format and extract time
            try {
                LocalDateTime dateTime = LocalDateTime.parse(source, DATETIME_FORMATTER);
                return dateTime.toLocalTime();
            } catch (DateTimeParseException e2) {
                // Try to parse as ISO datetime with optional seconds
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(source);
                    return dateTime.toLocalTime();
                } catch (DateTimeParseException e3) {
                    throw new IllegalArgumentException(
                            "Không thể chuyển đổi '" + source + "' thành LocalTime. " +
                            "Định dạng hỗ trợ: HH:mm (ví dụ: 14:30) hoặc ISO datetime (ví dụ: 2026-01-15T14:30:00)",
                            e3
                    );
                }
            }
        }
    }
}
