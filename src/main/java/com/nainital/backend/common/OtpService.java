package com.nainital.backend.common;

import com.nainital.backend.auth.repository.OtpRepository;
import com.nainital.backend.user.model.OtpRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${app.dev-mode:true}")
    private boolean devMode;

    private static final long OTP_EXPIRY_MINUTES = 10;

    /** Send OTP to email and return it in dev mode. */
    public String sendOtp(String email, String appName) {
        otpRepository.deleteByEmail(email);

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpRepository.save(OtpRecord.builder()
                .email(email)
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60))
                .used(false)
                .build());

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Your " + appName + " OTP");
            msg.setText("Your OTP is: " + otp + "\n\nThis OTP expires in " + OTP_EXPIRY_MINUTES + " minutes. Do not share it with anyone.");
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Email send failed for {}: {}", email, e.getMessage());
        }

        return devMode ? otp : null;
    }

    /** Verify OTP. Throws if invalid/expired. Deletes record on success. */
    public void verifyOtp(String email, String otp) {
        OtpRecord record = otpRepository.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found. Please request a new OTP."));

        if (record.isUsed()) {
            throw new IllegalArgumentException("OTP has already been used.");
        }
        if (Instant.now().isAfter(record.getExpiresAt())) {
            otpRepository.delete(record);
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }
        if (!record.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP. Please check and try again.");
        }

        otpRepository.delete(record);
    }
}
