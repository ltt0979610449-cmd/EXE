package swd.coiviet.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ← THÊM DÒNG NÀY
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/public/**", "/api/auth/**", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-ui.html", "/api/v1/shipments/**").permitAll()

                        // Upload endpoints: require authentication
                        .requestMatchers("/api/upload/**").hasAnyRole("USER", "STAFF", "ADMIN")

                        // Public tour/province endpoints (for browsing)
                        .requestMatchers("/api/tours/public/**", "/api/provinces/public/**",
                                "/api/culture-items/public/**", "/api/artisans/public/**").permitAll()

                        // Admin endpoints: chỉ ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Staff endpoints: STAFF hoặc ADMIN
                        .requestMatchers("/api/staff/**", "/api/tours", "/api/tours/**",
                                "/api/provinces", "/api/provinces/**", "/api/tour-schedules/**",
                                "/api/artisans", "/api/artisans/**", "/api/culture-items",
                                "/api/culture-items/**").hasAnyRole("STAFF", "ADMIN")

                        // User endpoints: USER, STAFF, ADMIN
                        .requestMatchers("/api/user/**", "/api/bookings/**").hasAnyRole("USER", "STAFF", "ADMIN")

                        // Các request khác: yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization"))
                        .addHeaders("ngrok-skip-browser-warning",
                                new io.swagger.v3.oas.models.headers.Header()
                                        .description("Bypass ngrok browser warning")
                                        .schema(new io.swagger.v3.oas.models.media.StringSchema().example("true"))))
                .info(new Info()
                        .title("CoiViet")
                        .version("1.0")
                        .description("API documentation for the CoiViet System"))
                .addServersItem(new Server().url("http://localhost:8080/"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:[*]");
        configuration.addAllowedOriginPattern("https://*.ngrok-free.app");
        configuration.addAllowedOriginPattern("http://localhost:8080");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
