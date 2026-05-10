package com.nainital.backend.coupon.service;

import com.nainital.backend.coupon.dto.CouponRequest;
import com.nainital.backend.coupon.dto.CouponValidateResponse;
import com.nainital.backend.coupon.model.Coupon;
import com.nainital.backend.coupon.model.DiscountType;
import com.nainital.backend.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository repo;

    public List<Coupon> getAll() { return repo.findAll(); }

    public Coupon getById(String id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
    }

    public Coupon create(CouponRequest req) {
        if (repo.existsByCode(req.getCode().toUpperCase()))
            throw new IllegalArgumentException("Coupon code already exists");
        return repo.save(Coupon.builder()
                .code(req.getCode().toUpperCase())
                .description(req.getDescription())
                .discountType(req.getDiscountType())
                .discountValue(req.getDiscountValue())
                .minOrderValue(req.getMinOrderValue())
                .maxDiscount(req.getMaxDiscount())
                .usageLimit(req.getUsageLimit())
                .storeCategory(req.getStoreCategory())
                .storeId(req.getStoreId())
                .active(req.isActive())
                .expiresAt(req.getExpiresAt())
                .build());
    }

    public Coupon update(String id, CouponRequest req) {
        Coupon c = getById(id);
        c.setCode(req.getCode().toUpperCase());
        c.setDescription(req.getDescription());
        c.setDiscountType(req.getDiscountType());
        c.setDiscountValue(req.getDiscountValue());
        c.setMinOrderValue(req.getMinOrderValue());
        c.setMaxDiscount(req.getMaxDiscount());
        c.setUsageLimit(req.getUsageLimit());
        c.setStoreCategory(req.getStoreCategory());
        c.setStoreId(req.getStoreId());
        c.setActive(req.isActive());
        c.setExpiresAt(req.getExpiresAt());
        return repo.save(c);
    }

    public void delete(String id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Coupon not found");
        repo.deleteById(id);
    }

    public Coupon toggleActive(String id, boolean active) {
        Coupon c = getById(id);
        c.setActive(active);
        return repo.save(c);
    }

    public CouponValidateResponse validate(String code, int orderTotal) {
        Coupon c = repo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        if (!c.isActive()) throw new IllegalStateException("Coupon is inactive");
        if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(java.time.Instant.now()))
            throw new IllegalStateException("Coupon has expired");
        if (orderTotal < c.getMinOrderValue())
            throw new IllegalStateException("Minimum order value is ₹" + c.getMinOrderValue());
        if (c.getUsageLimit() > 0 && c.getUsedCount() >= c.getUsageLimit())
            throw new IllegalStateException("Coupon usage limit reached");
        int discount = calcDiscount(c, orderTotal);
        return CouponValidateResponse.builder()
                .code(c.getCode())
                .discountType(c.getDiscountType().name())
                .discountValue(c.getDiscountValue())
                .maxDiscount(c.getMaxDiscount())
                .minOrderValue(c.getMinOrderValue())
                .discountAmount(discount)
                .description(c.getDescription())
                .build();
    }

    // Compute actual rupee discount for a given subtotal
    public int calcDiscount(Coupon c, int subtotal) {
        if (c.getDiscountType() == DiscountType.FLAT) {
            return Math.min(c.getDiscountValue(), subtotal);
        }
        // PERCENT
        int raw = (int) Math.round(subtotal * c.getDiscountValue() / 100.0);
        return (c.getMaxDiscount() > 0) ? Math.min(raw, c.getMaxDiscount()) : raw;
    }

    // Validates + returns the Coupon entity for use in OrderService
    public Coupon validateAndGet(String code, int subtotal) {
        Coupon c = repo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code"));
        if (!c.isActive()) throw new IllegalStateException("Coupon is inactive");
        if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(java.time.Instant.now()))
            throw new IllegalStateException("Coupon has expired");
        if (subtotal < c.getMinOrderValue())
            throw new IllegalStateException("Minimum order value is ₹" + c.getMinOrderValue());
        if (c.getUsageLimit() > 0 && c.getUsedCount() >= c.getUsageLimit())
            throw new IllegalStateException("Coupon usage limit reached");
        return c;
    }

    // Increments usedCount — call after order is confirmed
    public void incrementUsage(String code) {
        repo.findByCode(code.toUpperCase()).ifPresent(c -> {
            c.setUsedCount(c.getUsedCount() + 1);
            repo.save(c);
        });
    }
}
