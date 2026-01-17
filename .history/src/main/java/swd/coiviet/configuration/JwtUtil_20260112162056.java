package swd.coiviet.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import swd.coiviet.exception.AppException;
import swd.coiviet.exception.ErrorCode;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.signerKey}")
    private String secret;

    @Value("${jwt.valid-duration}")
    private long expiration; // seconds

    @Value("${jwt.refreshable-duration}")
    private long refreshableDuration;

    public String generateToken(String username, String role, Integer userId) {
        long expirationInMillis = expiration * 1000;
        logger.info("Generating token with expiration: " + expirationInMillis + " ms (" + expiration + " seconds)");
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .setAllowedClockSkewSeconds(60)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .setAllowedClockSkewSeconds(60)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .setAllowedClockSkewSeconds(60)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Integer.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                    .setAllowedClockSkewSeconds(60)
                    .parseClaimsJws(token)
                    .getBody();
            boolean isExpired = isTokenExpired(claims);
            if (isExpired) {
                logger.warn("Token is expired - exp: " + claims.getExpiration() + ", current time: " + new Date());
            }
            return !isExpired;
        } catch (Exception e) {
            logger.error("Error validating token: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public boolean canRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                    .setAllowedClockSkewSeconds(60)
                    .parseClaimsJws(token)
                    .getBody();
            Date issuedAt = claims.getIssuedAt();
            Date refreshableUntil = new Date(issuedAt.getTime() + refreshableDuration);
            return new Date().before(refreshableUntil);
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        if (!canRefreshToken(token)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Token cannot be refreshed");
        }
        Claims claims = Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .parseClaimsJws(token)
                .getBody();
        return generateToken(claims.getSubject(), claims.get("role", String.class), claims.get("userId", Integer.class));
    }

    public String getSecret() {
        return secret;
    }
}
