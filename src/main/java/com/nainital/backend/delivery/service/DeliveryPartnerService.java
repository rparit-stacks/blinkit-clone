package com.nainital.backend.delivery.service;

import com.nainital.backend.delivery.dto.DeliveryPartnerRequest;
import com.nainital.backend.delivery.dto.DeliveryRegisterRequest;
import com.nainital.backend.delivery.dto.UpdateDeliveryProfileRequest;
import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.model.PartnerStatus;
import com.nainital.backend.delivery.repository.DeliveryPartnerRepository;
import com.nainital.backend.notification.service.NotificationPublisher;
import com.nainital.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationPublisher notificationPublisher;

    // ─── Admin CRUD ───────────────────────────────────────────────────────────

    public List<DeliveryPartner> getAll() { return repo.findAll(); }

    public DeliveryPartner getById(String id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Partner not found"));
    }

    public DeliveryPartner create(DeliveryPartnerRequest req) {
        if (repo.existsByPhone(req.getPhone()))
            throw new IllegalArgumentException("Phone already registered");
        return repo.save(DeliveryPartner.builder()
                .name(req.getName()).phone(req.getPhone())
                .email(req.getEmail()).vehicleType(req.getVehicleType())
                .vehicleNumber(req.getVehicleNumber())
                .status(PartnerStatus.PENDING)
                .build());
    }

    public DeliveryPartner update(String id, DeliveryPartnerRequest req) {
        DeliveryPartner p = getById(id);
        p.setName(req.getName()); p.setPhone(req.getPhone());
        p.setEmail(req.getEmail()); p.setVehicleType(req.getVehicleType());
        p.setVehicleNumber(req.getVehicleNumber());
        return repo.save(p);
    }

    public DeliveryPartner approve(String id) {
        DeliveryPartner p = getById(id);

        // Require at least one KYC document before approval
        boolean hasKyc = p.getIdProofUrl() != null || p.getLicenseUrl() != null || p.getVehicleImageUrl() != null;
        if (!hasKyc) {
            throw new IllegalStateException("Cannot approve: delivery partner has not uploaded any KYC documents.");
        }

        p.setStatus(PartnerStatus.APPROVED);
        p = repo.save(p);
        notificationPublisher.deliveryPartnerApproved(p.getId());
        return p;
    }

    public DeliveryPartner block(String id, boolean block) {
        DeliveryPartner p = getById(id);
        p.setStatus(block ? PartnerStatus.BLOCKED : PartnerStatus.APPROVED);
        p = repo.save(p);
        notificationPublisher.deliveryPartnerBlocked(p.getId(), block);
        return p;
    }

    public void delete(String id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Partner not found");
        repo.deleteById(id);
    }

    // ─── Auth (delivery partner self-service) ─────────────────────────────────

    public DeliveryPartner register(DeliveryRegisterRequest req) {
        if (repo.existsByPhone(req.getPhone()))
            throw new IllegalArgumentException("Phone already registered");
        DeliveryPartner p = repo.save(DeliveryPartner.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .vehicleType(req.getVehicleType())
                .vehicleNumber(req.getVehicleNumber())
                .status(PartnerStatus.PENDING)
                .build());
        notificationPublisher.deliveryPartnerRegistered(p.getId(), p.getName());
        return p;
    }

    /**
     * Validates phone+password and returns a JWT token string.
     */
    public String login(String phone, String password) {
        DeliveryPartner partner = repo.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Invalid phone or password"));
        if (partner.getPassword() == null || !passwordEncoder.matches(password, partner.getPassword())) {
            throw new IllegalArgumentException("Invalid phone or password");
        }
        if (partner.getStatus() == PartnerStatus.BLOCKED) {
            throw new IllegalStateException("Your account has been blocked. Contact support.");
        }
        return jwtUtil.generateDeliveryToken(partner.getId(), partner.getPhone());
    }

    public DeliveryPartner getProfile(String partnerId) {
        return getById(partnerId);
    }

    public DeliveryPartner updateProfile(String partnerId, UpdateDeliveryProfileRequest req) {
        DeliveryPartner p = getById(partnerId);
        if (req.getName() != null) p.setName(req.getName());
        if (req.getEmail() != null) p.setEmail(req.getEmail());
        if (req.getProfileImage() != null) p.setProfileImage(req.getProfileImage());
        if (req.getVehicleType() != null) p.setVehicleType(req.getVehicleType());
        if (req.getVehicleNumber() != null) p.setVehicleNumber(req.getVehicleNumber());
        if (req.getIdProofUrl() != null) p.setIdProofUrl(req.getIdProofUrl());
        if (req.getVehicleImageUrl() != null) p.setVehicleImageUrl(req.getVehicleImageUrl());
        if (req.getLicenseUrl() != null) p.setLicenseUrl(req.getLicenseUrl());
        if (req.getBankAccountNumber() != null) p.setBankAccountNumber(req.getBankAccountNumber());
        if (req.getBankIfsc() != null) p.setBankIfsc(req.getBankIfsc());
        if (req.getBankAccountHolderName() != null) p.setBankAccountHolderName(req.getBankAccountHolderName());
        if (req.getBankName() != null) p.setBankName(req.getBankName());
        if (req.getUpiId() != null) p.setUpiId(req.getUpiId());
        if (req.getCurrentLatitude() != null) p.setCurrentLatitude(req.getCurrentLatitude());
        if (req.getCurrentLongitude() != null) p.setCurrentLongitude(req.getCurrentLongitude());
        return repo.save(p);
    }

    public DeliveryPartner toggleOnline(String partnerId) {
        DeliveryPartner p = getById(partnerId);
        if (p.getStatus() != PartnerStatus.APPROVED) {
            throw new IllegalStateException("Only approved partners can go online");
        }
        p.setOnline(!p.isOnline());
        return repo.save(p);
    }

    public void registerFcmToken(String partnerId, String token) {
        DeliveryPartner p = getById(partnerId);
        List<String> tokens = p.getFcmTokens() != null ? new ArrayList<>(p.getFcmTokens()) : new ArrayList<>();
        if (!tokens.contains(token)) {
            tokens.add(token);
            if (tokens.size() > 10) {
                tokens = new ArrayList<>(tokens.subList(tokens.size() - 10, tokens.size()));
            }
        }
        p.setFcmTokens(tokens);
        repo.save(p);
    }

    public void removeFcmToken(String partnerId, String token) {
        DeliveryPartner p = getById(partnerId);
        List<String> tokens = p.getFcmTokens();
        if (tokens != null && tokens.remove(token)) {
            p.setFcmTokens(tokens);
            repo.save(p);
        }
    }
}
