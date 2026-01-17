package swd.coiviet.configuration;

import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import swd.coiviet.enums.Status;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;
import swd.coiviet.repository.UserRepository;

import java.util.Collections;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtChannelInterceptor(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);
            if (token == null || token.isBlank()) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Missing JWT token");
            }
            if (!jwtUtil.validateToken(token)) {
                throw new AppException(ErrorCode.TOKEN_INVALID, "Token is invalid or expired");
            }

            Claims claims = jwtUtil.getClaims(token);
            Integer userId = claims.get("userId", Integer.class);
            String role = claims.get("role", String.class);

            if (userId == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Token missing userId");
            }

            swd.coiviet.model.User user = userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));
            if (user.getStatus() == Status.BANNED) {
                throw new AppException(ErrorCode.FORBIDDEN, "Account is banned");
            }

            org.springframework.security.core.userdetails.User userDetails =
                    new org.springframework.security.core.userdetails.User(
                            String.valueOf(userId),
                            "",
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, token, userDetails.getAuthorities());
            accessor.setUser(authentication);
        }

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null) {
            header = accessor.getFirstNativeHeader("authorization");
        }
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (header != null && !header.isBlank()) {
            return header;
        }
        String token = accessor.getFirstNativeHeader("token");
        if (token != null && !token.isBlank()) {
            return token;
        }
        return null;
    }
}
