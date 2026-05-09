package com.nainital.backend.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nainital.backend.auth.dto.AuthResponse;
import com.nainital.backend.auth.dto.SendOtpResponse;
import com.nainital.backend.auth.repository.OtpRepository;
import com.nainital.backend.auth.repository.RefreshTokenRepository;
import com.nainital.backend.security.JwtUtil;
import com.nainital.backend.user.model.OtpRecord;
import com.nainital.backend.user.model.RefreshToken;
import com.nainital.backend.user.model.User;
import com.nainital.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    @Value("${app.dev-mode:true}")
    private boolean devMode;

    @Value("${google.client-id:}")
    private String googleClientId;

    private static final long OTP_EXPIRY_MINUTES = 10;

    // ─────────────────────────────────────────────
    // Email OTP: Send
    // ─────────────────────────────────────────────

    public SendOtpResponse sendOtp(String email) {
        // Delete any existing OTP for this email
        otpRepository.deleteByEmail(email);

        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60);

        OtpRecord record = OtpRecord.builder()
                .email(email)
                .otp(otp)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        otpRepository.save(record);

        // Send email (best effort — don't fail request if mail not configured)
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your NaniStore OTP");
            message.setText("Your OTP is: " + otp + "\n\nThis OTP expires in 10 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            // Log but don't throw — in dev mode OTP is returned in response
        }

        SendOtpResponse.SendOtpResponseBuilder builder = SendOtpResponse.builder()
                .message("OTP sent successfully");

        if (devMode) {
            builder.otp(otp);
        }

        return builder.build();
    }

    // ─────────────────────────────────────────────
    // Email OTP: Verify
    // ─────────────────────────────────────────────

    public AuthResponse verifyOtp(String email, String otp) {
        OtpRecord record = otpRepository.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP not found. Please request a new OTP."));

        if (record.isUsed()) {
            throw new RuntimeException("OTP has already been used.");
        }

        if (Instant.now().isAfter(record.getExpiresAt())) {
            otpRepository.delete(record);
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        if (!record.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP.");
        }

        // Mark as used and delete
        otpRepository.delete(record);

        // Find or create user
        boolean isNewUser = false;
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .role("CUSTOMER")
                    .onboardingCompleted(false)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;
        }

        return buildAuthResponse(user, isNewUser);
    }

    // ─────────────────────────────────────────────
    // Google OAuth
    // ─────────────────────────────────────────────

    public AuthResponse googleAuth(String idTokenString) {
        GoogleIdToken.Payload payload = verifyGoogleIdToken(idTokenString);

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        boolean isNewUser = false;
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .name(name)
                    .profileImage(picture)
                    .role("CUSTOMER")
                    .onboardingCompleted(false)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;
        }

        return buildAuthResponse(user, isNewUser);
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier.Builder verifierBuilder = new GoogleIdTokenVerifier.Builder(transport, jsonFactory);

            if (googleClientId != null && !googleClientId.isBlank()) {
                verifierBuilder.setAudience(Collections.singletonList(googleClientId));
            }

            GoogleIdTokenVerifier verifier = verifierBuilder.build();
            GoogleIdToken googleIdToken = verifier.verify(idTokenString);

            if (googleIdToken == null) {
                throw new RuntimeException("Invalid Google ID token.");
            }

            return googleIdToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to verify Google ID token: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Token Refresh
    // ─────────────────────────────────────────────

    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token not found."));

        if (stored.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked.");
        }

        if (Instant.now().isAfter(stored.getExpiresAt())) {
            refreshTokenRepository.delete(stored);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Rotate: delete old, issue new
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(user, false);
    }

    // ─────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────

    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, boolean isNewUser) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String rawRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        long refreshExpiryMs = Long.parseLong(
                Optional.ofNullable(System.getenv("JWT_REFRESH_EXPIRY_MS")).orElse("2592000000"));

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(rawRefreshToken)
                .userId(user.getId())
                .expiresAt(Instant.now().plusMillis(refreshExpiryMs))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .isNewUser(isNewUser)
                .build();
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
