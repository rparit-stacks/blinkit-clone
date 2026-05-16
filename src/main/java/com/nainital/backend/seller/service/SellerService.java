package com.nainital.backend.seller.service;

import com.nainital.backend.catalog.model.Product;
import com.nainital.backend.catalog.model.Store;
import com.nainital.backend.catalog.model.StoreCategory;
import com.nainital.backend.catalog.repository.ProductRepository;
import com.nainital.backend.catalog.repository.StoreRepository;
import com.nainital.backend.order.model.Order;
import com.nainital.backend.order.model.OrderStatus;
import com.nainital.backend.order.repository.OrderRepository;
import com.nainital.backend.notification.service.NotificationPublisher;
import com.nainital.backend.security.JwtUtil;
import com.nainital.backend.seller.dto.*;
import com.nainital.backend.seller.model.Seller;
import com.nainital.backend.seller.model.SellerProduct;
import com.nainital.backend.seller.model.SellerStatus;
import com.nainital.backend.seller.repository.SellerProductRepository;
import com.nainital.backend.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerProductRepository sellerProductRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final NotificationPublisher notificationPublisher;

    // ─── Auth ────────────────────────────────────────────────────────────────

    public SellerAuthResponse register(SellerRegisterRequest req) {
        if (sellerRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        Seller seller = Seller.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .storeName(req.getStoreName())
                .storeCategory(req.getStoreCategory())
                .description(req.getDescription())
                .gstNumber(req.getGstNumber())
                .panNumber(req.getPanNumber())
                .businessRegNumber(req.getBusinessRegNumber())
                .addressLine(req.getAddressLine())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .bankAccountNumber(req.getBankAccountNumber())
                .bankIfsc(req.getBankIfsc())
                .bankAccountHolderName(req.getBankAccountHolderName())
                .bankName(req.getBankName())
                .status(SellerStatus.PENDING)
                .storeOpen(false)
                .build();

        seller = sellerRepository.save(seller);
        notificationPublisher.sellerRegistered(seller.getId(), seller.getStoreName());
        String token = jwtUtil.generateSellerToken(seller.getId(), seller.getEmail());
        return toAuthResponse(seller, token);
    }

    public SellerAuthResponse login(SellerLoginRequest req) {
        Seller seller = sellerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), seller.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateSellerToken(seller.getId(), seller.getEmail());
        return toAuthResponse(seller, token);
    }

    // ─── Profile ─────────────────────────────────────────────────────────────

    public SellerProfileDto getProfile(String sellerId) {
        Seller s = getById(sellerId);
        return toProfileDto(s);
    }

    public SellerProfileDto updateProfile(String sellerId, SellerUpdateRequest req) {
        Seller s = getById(sellerId);
        if (req.getFullName() != null) s.setFullName(req.getFullName());
        if (req.getPhone() != null) s.setPhone(req.getPhone());
        if (req.getDescription() != null) s.setDescription(req.getDescription());
        if (req.getAddressLine() != null) s.setAddressLine(req.getAddressLine());
        if (req.getCity() != null) s.setCity(req.getCity());
        if (req.getState() != null) s.setState(req.getState());
        if (req.getPincode() != null) s.setPincode(req.getPincode());
        if (req.getLatitude() != null) s.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) s.setLongitude(req.getLongitude());
        if (req.getOpenTime() != null) s.setOpenTime(req.getOpenTime());
        if (req.getCloseTime() != null) s.setCloseTime(req.getCloseTime());
        if (req.getLogoUrl() != null) s.setLogoUrl(req.getLogoUrl());
        if (req.getBannerUrl() != null) s.setBannerUrl(req.getBannerUrl());
        if (req.getStoreImages() != null) s.setStoreImages(req.getStoreImages());
        if (req.getStoreOpen() != null) s.setStoreOpen(req.getStoreOpen());
        s = sellerRepository.save(s);

        // Sync open/close status to the linked Store if exists
        if (s.getStoreId() != null) {
            storeRepository.findById(s.getStoreId()).ifPresent(store -> {
                if (req.getStoreOpen() != null) store.setActive(req.getStoreOpen());
                if (req.getDescription() != null) store.setDescription(req.getDescription());
                if (req.getLogoUrl() != null) store.setImage(req.getLogoUrl());
                if (req.getBannerUrl() != null) store.setCoverImage(req.getBannerUrl());
                storeRepository.save(store);
            });
        }
        return toProfileDto(s);
    }

    public void updateDocuments(String sellerId, String field, String url) {
        Seller s = getById(sellerId);
        switch (field) {
            case "gstCertificateUrl" -> s.setGstCertificateUrl(url);
            case "panCardUrl" -> s.setPanCardUrl(url);
            case "licenseUrl" -> s.setLicenseUrl(url);
            case "businessProofUrl" -> s.setBusinessProofUrl(url);
            case "idProofUrl" -> s.setIdProofUrl(url);
            case "logoUrl" -> s.setLogoUrl(url);
            case "bannerUrl" -> s.setBannerUrl(url);
            default -> throw new IllegalArgumentException("Unknown document field: " + field);
        }
        sellerRepository.save(s);
    }

    // ─── Store toggle ────────────────────────────────────────────────────────

    public SellerProfileDto toggleStore(String sellerId) {
        Seller s = getById(sellerId);
        requireApproved(s);
        s.setStoreOpen(!s.isStoreOpen());
        s = sellerRepository.save(s);
        final boolean open = s.isStoreOpen();
        if (s.getStoreId() != null) {
            storeRepository.findById(s.getStoreId()).ifPresent(store -> {
                store.setActive(open);
                storeRepository.save(store);
            });
        }
        return toProfileDto(s);
    }

    // ─── Products ────────────────────────────────────────────────────────────

    public SellerProduct addProduct(String sellerId, SellerProductRequest req) {
        Seller s = getById(sellerId);
        requireApproved(s);

        // Create the catalog Product
        Product product = Product.builder()
                .storeCategory(StoreCategory.valueOf(s.getStoreCategory().toUpperCase()))
                .storeId(s.getStoreId())
                .categorySlug(req.getCategorySlug())
                .name(req.getName())
                .description(req.getDescription())
                .image(req.getImage())
                .images(req.getImages())
                .price(req.getPrice())
                .originalPrice(req.getOriginalPrice())
                .unit(req.getUnit())
                .badge(req.getBadge())
                .available(req.isAvailable())
                .approved(false) // needs admin approval
                .sortOrder(0)
                .build();
        product = productRepository.save(product);

        // Create the SellerProduct record
        SellerProduct sp = SellerProduct.builder()
                .sellerId(sellerId)
                .productId(product.getId())
                .storeId(s.getStoreId())
                .name(req.getName())
                .description(req.getDescription())
                .categorySlug(req.getCategorySlug())
                .storeCategory(s.getStoreCategory())
                .price(req.getPrice())
                .originalPrice(req.getOriginalPrice())
                .unit(req.getUnit())
                .badge(req.getBadge())
                .sku(req.getSku())
                .stockQuantity(req.getStockQuantity())
                .gstPercent(req.getGstPercent())
                .image(req.getImage())
                .images(req.getImages())
                .tags(req.getTags())
                .available(req.isAvailable())
                .build();
        return sellerProductRepository.save(sp);
    }

    public SellerProduct updateProduct(String sellerId, String productId, SellerProductRequest req) {
        SellerProduct sp = sellerProductRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        sp.setName(req.getName());
        sp.setDescription(req.getDescription());
        sp.setCategorySlug(req.getCategorySlug());
        sp.setPrice(req.getPrice());
        sp.setOriginalPrice(req.getOriginalPrice());
        sp.setUnit(req.getUnit());
        sp.setBadge(req.getBadge());
        sp.setSku(req.getSku());
        sp.setStockQuantity(req.getStockQuantity());
        sp.setGstPercent(req.getGstPercent());
        sp.setImage(req.getImage());
        sp.setImages(req.getImages());
        sp.setTags(req.getTags());
        sp.setAvailable(req.isAvailable());

        // Sync to catalog Product
        if (sp.getProductId() != null) {
            productRepository.findById(sp.getProductId()).ifPresent(p -> {
                p.setName(req.getName());
                p.setDescription(req.getDescription());
                p.setImage(req.getImage());
                p.setImages(req.getImages());
                p.setPrice(req.getPrice());
                p.setOriginalPrice(req.getOriginalPrice());
                p.setUnit(req.getUnit());
                p.setBadge(req.getBadge());
                p.setAvailable(req.isAvailable());
                p.setCategorySlug(req.getCategorySlug());
                productRepository.save(p);
            });
        }
        return sellerProductRepository.save(sp);
    }

    public void deleteProduct(String sellerId, String productId) {
        SellerProduct sp = sellerProductRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        // Soft-delete from catalog
        if (sp.getProductId() != null) {
            productRepository.findById(sp.getProductId()).ifPresent(p -> {
                p.setAvailable(false);
                p.setApproved(false);
                productRepository.save(p);
            });
        }
        sellerProductRepository.delete(sp);
    }

    public List<SellerProduct> getProducts(String sellerId) {
        return sellerProductRepository.findAllBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    // ─── Orders ──────────────────────────────────────────────────────────────

    public List<Order> getOrders(String sellerId) {
        Seller s = getById(sellerId);
        if (s.getStoreId() == null) return List.of();
        return orderRepository.findAllByStoreIdOrderByCreatedAtDesc(s.getStoreId());
    }

    public Order updateOrderStatus(String sellerId, String orderId, String status) {
        Seller s = getById(sellerId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // Verify the order belongs to this seller's store
        boolean belongs = order.getItems().stream()
                .anyMatch(item -> s.getStoreId() != null && s.getStoreId().equals(item.getStoreId()));
        if (!belongs) throw new IllegalArgumentException("Order not found");
        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        return orderRepository.save(order);
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    public SellerDashboardDto getDashboard(String sellerId) {
        Seller s = getById(sellerId);
        List<Order> orders = s.getStoreId() != null
                ? orderRepository.findAllByStoreIdOrderByCreatedAtDesc(s.getStoreId())
                : List.of();

        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);

        long total = orders.size();
        long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.PROCESSING).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long revenue = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).mapToLong(Order::getTotal).sum();
        long todayOrders = orders.stream().filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart)).count();
        long todayRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart))
                .mapToLong(Order::getTotal).sum();

        long totalProducts = sellerProductRepository.countBySellerId(sellerId);
        long activeProducts = sellerProductRepository.countBySellerIdAndAvailable(sellerId, true);

        return SellerDashboardDto.builder()
                .totalOrders(total)
                .pendingOrders(pending)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .totalRevenue(revenue)
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .build();
    }

    // ─── Admin: seller management ────────────────────────────────────────────

    public List<Seller> getAllSellers() {
        return sellerRepository.findAllByOrderByCreatedAtDesc();
    }

    public Seller approveSeller(String sellerId) {
        Seller s = getById(sellerId);

        // Require at least one KYC document and a profile photo before approval
        boolean hasKyc = s.getIdProofUrl() != null || s.getPanCardUrl() != null
                || s.getGstCertificateUrl() != null || s.getBusinessProofUrl() != null;
        if (!hasKyc) {
            throw new IllegalStateException("Cannot approve: seller has not uploaded any KYC documents.");
        }

        s.setStatus(SellerStatus.APPROVED);
        s.setRejectionReason(null);
        s = sellerRepository.save(s);

        // Auto-create a Store for this seller if none exists
        if (s.getStoreId() == null) {
            StoreCategory cat;
            try {
                cat = StoreCategory.valueOf(s.getStoreCategory().toUpperCase());
            } catch (Exception e) {
                cat = StoreCategory.BAZAAR;
            }
            Store store = Store.builder()
                    .name(s.getStoreName())
                    .description(s.getDescription())
                    .storeCategory(cat)
                    .image(s.getLogoUrl())
                    .coverImage(s.getBannerUrl())
                    .address(s.getAddressLine() + ", " + s.getCity())
                    .phone(s.getPhone())
                    .active(false)
                    .sortOrder(0)
                    .build();
            store = storeRepository.save(store);
            s.setStoreId(store.getId());
            s = sellerRepository.save(s);
        }
        notificationPublisher.sellerApproved(s.getId());
        return s;
    }

    public Seller rejectSeller(String sellerId, String reason) {
        Seller s = getById(sellerId);
        s.setStatus(SellerStatus.REJECTED);
        s.setRejectionReason(reason);
        s = sellerRepository.save(s);
        notificationPublisher.sellerRejected(s.getId(), reason);
        return s;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Seller getById(String id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
    }

    private void requireApproved(Seller s) {
        if (s.getStatus() != SellerStatus.APPROVED) {
            throw new IllegalStateException("Seller account is not approved yet");
        }
    }

    private SellerAuthResponse toAuthResponse(Seller s, String token) {
        return SellerAuthResponse.builder()
                .token(token)
                .sellerId(s.getId())
                .email(s.getEmail())
                .fullName(s.getFullName())
                .storeName(s.getStoreName())
                .storeCategory(s.getStoreCategory())
                .status(s.getStatus().name())
                .storeId(s.getStoreId())
                .build();
    }

    private SellerProfileDto toProfileDto(Seller s) {
        return SellerProfileDto.builder()
                .id(s.getId())
                .email(s.getEmail())
                .fullName(s.getFullName())
                .phone(s.getPhone())
                .storeName(s.getStoreName())
                .storeCategory(s.getStoreCategory())
                .description(s.getDescription())
                .gstNumber(s.getGstNumber())
                .panNumber(s.getPanNumber())
                .businessRegNumber(s.getBusinessRegNumber())
                .addressLine(s.getAddressLine())
                .city(s.getCity())
                .state(s.getState())
                .pincode(s.getPincode())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .bankAccountNumber(s.getBankAccountNumber())
                .bankIfsc(s.getBankIfsc())
                .bankAccountHolderName(s.getBankAccountHolderName())
                .bankName(s.getBankName())
                .gstCertificateUrl(s.getGstCertificateUrl())
                .panCardUrl(s.getPanCardUrl())
                .licenseUrl(s.getLicenseUrl())
                .businessProofUrl(s.getBusinessProofUrl())
                .idProofUrl(s.getIdProofUrl())
                .logoUrl(s.getLogoUrl())
                .bannerUrl(s.getBannerUrl())
                .storeImages(s.getStoreImages())
                .storeId(s.getStoreId())
                .status(s.getStatus().name())
                .rejectionReason(s.getRejectionReason())
                .storeOpen(s.isStoreOpen())
                .openTime(s.getOpenTime())
                .closeTime(s.getCloseTime())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
