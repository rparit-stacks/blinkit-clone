package com.nainital.backend.delivery.service;

import com.nainital.backend.delivery.dto.DeliveryPartnerRequest;
import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.model.PartnerStatus;
import com.nainital.backend.delivery.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository repo;

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
        p.setStatus(PartnerStatus.APPROVED);
        return repo.save(p);
    }

    public DeliveryPartner block(String id, boolean block) {
        DeliveryPartner p = getById(id);
        p.setStatus(block ? PartnerStatus.BLOCKED : PartnerStatus.APPROVED);
        return repo.save(p);
    }

    public void delete(String id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Partner not found");
        repo.deleteById(id);
    }
}
