package com.nainital.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
            @Value("${jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs) {

        // Ensure the key is at least 256 bits (32 bytes)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public String generateAccessToken(String userId, String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("email", email, "role", role))
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExpiryMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateSellerToken(String sellerId, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(sellerId)
                .claims(Map.of("email", email, "role", "SELLER", "type", "seller"))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 8 * 3600 * 1000L)) // 8 hours
                .signWith(secretKey)
                .compact();
    }

    public boolean isSellerToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "seller".equals(type);
        } catch (Exception e) { return false; }
    }

    public String generateAdminToken(String adminId, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(adminId)
                .claims(Map.of("role", role, "type", "admin"))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 8 * 3600 * 1000L)) // 8 hours
                .signWith(secretKey)
                .compact();
    }

    public boolean isAdminToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "admin".equals(type);
        } catch (Exception e) { return false; }
    }

    public String generateDeliveryToken(String partnerId, String phone) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(partnerId)
                .claims(Map.of("phone", phone, "role", "DELIVERY", "type", "delivery"))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 8 * 3600 * 1000L)) // 8 hours
                .signWith(secretKey)
                .compact();
    }

    public boolean isDeliveryToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "delivery".equals(type);
        } catch (Exception e) { return false; }
    }

    public String generateRefreshToken(String userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId)
                .claim("type", "refresh")
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTokenExpiryMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
