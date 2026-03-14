package swd.coiviet.configuration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Group mặc định - hiển thị TẤT CẢ API khi người dùng không chọn nhóm cụ thể.
     */
    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("Auth")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("Payment")
                .pathsToMatch("/api/payments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi learnApi() {
        return GroupedOpenApi.builder()
                .group("Learn")
                .pathsToMatch("/api/learn/**")
                .build();
    }

    @Bean
    public GroupedOpenApi tourApi() {
        return GroupedOpenApi.builder()
                .group("Tour")
                .pathsToMatch("/api/tours/**", "/api/tour-schedules/**")
                .build();
    }

    @Bean
    public GroupedOpenApi bookingApi() {
        return GroupedOpenApi.builder()
                .group("Booking")
                .pathsToMatch("/api/bookings/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("User")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi artisanApi() {
        return GroupedOpenApi.builder()
                .group("Artisan")
                .pathsToMatch("/api/artisans/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("Admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi contentApi() {
        return GroupedOpenApi.builder()
                .group("Content")
                .pathsToMatch("/api/blog-posts/**", "/api/videos/**", "/api/culture-items/**", "/api/user-memories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi aiChatApi() {
        return GroupedOpenApi.builder()
                .group("AI Chat")
                .pathsToMatch("/api/ai-chat/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("Notification")
                .pathsToMatch("/api/notifications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi voucherApi() {
        return GroupedOpenApi.builder()
                .group("Voucher")
                .pathsToMatch("/api/vouchers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder()
                .group("Review")
                .pathsToMatch("/api/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi provinceApi() {
        return GroupedOpenApi.builder()
                .group("Province")
                .pathsToMatch("/api/provinces/**")
                .build();
    }

    @Bean
    public GroupedOpenApi uploadApi() {
        return GroupedOpenApi.builder()
                .group("Upload")
                .pathsToMatch("/api/upload/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("Public")
                .pathsToMatch("/api/public/**")
                .build();
    }

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
                .group("Chat")
                .pathsToMatch("/api/chats/**")
                .build();
    }

    @Bean
    public GroupedOpenApi leadApi() {
        return GroupedOpenApi.builder()
                .group("Lead")
                .pathsToMatch("/api/leads/**")
                .build();
    }

    @Bean
    public GroupedOpenApi trackApi() {
        return GroupedOpenApi.builder()
                .group("Track")
                .pathsToMatch("/api/track/**")
                .build();
    }
}
