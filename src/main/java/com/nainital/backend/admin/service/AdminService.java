package com.nainital.backend.admin.service;

import com.nainital.backend.admin.dto.AdminCreateUserRequest;
import com.nainital.backend.admin.dto.AdminLoginRequest;
import com.nainital.backend.admin.dto.AdminLoginResponse;
import com.nainital.backend.admin.dto.AdminUpdateUserRequest;
import com.nainital.backend.admin.dto.CreateAdminRequest;
import com.nainital.backend.admin.dto.DashboardStats;
import com.nainital.backend.admin.model.Admin;
import com.nainital.backend.admin.model.AdminRole;
import com.nainital.backend.admin.repository.AdminRepository;
import com.nainital.backend.catalog.repository.ProductRepository;
import com.nainital.backend.catalog.repository.StoreRepository;
import com.nainital.backend.coupon.repository.CouponRepository;
import com.nainital.backend.delivery.repository.DeliveryPartnerRepository;
import com.nainital.backend.order.model.OrderStatus;
import com.nainital.backend.order.repository.OrderRepository;
import com.nainital.backend.security.JwtUtil;
import com.nainital.backend.user.model.User;
import com.nainital.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final StoreRepository storeRepo;
    private final DeliveryPartnerRepository deliveryRepo;
    private final CouponRepository couponRepo;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ─── Auth ─────────────────────────────────────────────────────────────────

    public AdminLoginResponse login(AdminLoginRequest req) {
        Admin admin = adminRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!admin.isActive()) throw new IllegalStateException("Account is disabled");
        if (!encoder.matches(req.getPassword(), admin.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getRole().name());
        return AdminLoginResponse.builder()
                .token(token).adminId(admin.getId())
                .email(admin.getEmail()).name(admin.getName()).role(admin.getRole())
                .build();
    }

    public Admin createAdmin(CreateAdminRequest req) {
        if (adminRepo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already exists");
        return adminRepo.save(Admin.builder()
                .email(req.getEmail())
                .passwordHash(encoder.encode(req.getPassword()))
                .name(req.getName())
                .role(req.getRole() != null ? req.getRole() : AdminRole.SUPPORT)
                .build());
    }

    public void bootstrapSuperAdmin() {
        if (!adminRepo.existsByEmail("admin@nainital.com")) {
            adminRepo.save(Admin.builder()
                    .email("admin@nainital.com")
                    .passwordHash(encoder.encode("Admin@123"))
                    .name("Super Admin")
                    .role(AdminRole.SUPER_ADMIN)
                    .build());
        }
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    public DashboardStats getDashboardStats() {
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);

        long totalOrders = orderRepo.count();
        long pendingOrders = orderRepo.countByStatus(OrderStatus.PENDING);
        long processingOrders = orderRepo.countByStatus(OrderStatus.PROCESSING);
        long deliveredOrders = orderRepo.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepo.countByStatus(OrderStatus.CANCELLED);

        long totalRevenue = orderRepo.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.PROCESSING)
                .mapToLong(o -> o.getTotal()).sum();

        long todayOrders = orderRepo.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart))
                .count();
        long todayRevenue = orderRepo.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart))
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.PROCESSING)
                .mapToLong(o -> o.getTotal()).sum();

        return DashboardStats.builder()
                .totalUsers(userRepo.count())
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .processingOrders(processingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalProducts(productRepo.count())
                .totalStores(storeRepo.count())
                .totalDeliveryPartners(deliveryRepo.count())
                .totalCoupons(couponRepo.count())
                .totalRevenue(totalRevenue)
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .build();
    }

    // ─── User management ──────────────────────────────────────────────────────

    public List<User> getAllUsers() { return userRepo.findAll(); }

    public User getUser(String id) {
        return userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User blockUser(String id, boolean block) {
        User u = getUser(id);
        u.setRole(block ? "BLOCKED" : "CUSTOMER");
        return userRepo.save(u);
    }

    public void deleteUser(String id) {
        if (!userRepo.existsById(id)) throw new IllegalArgumentException("User not found");
        userRepo.deleteById(id);
    }

    public User createUser(AdminCreateUserRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required");
        String email = req.getEmail().trim();
        if (userRepo.existsByEmail(email))
            throw new IllegalArgumentException("Email already exists");
        return userRepo.save(User.builder()
                .email(email)
                .name(req.getName() != null ? req.getName().trim() : "")
                .phone(req.getPhone() != null && !req.getPhone().isBlank() ? req.getPhone().trim() : null)
                .role("CUSTOMER")
                .onboardingCompleted(true)
                .build());
    }

    public User updateUser(String id, AdminUpdateUserRequest req) {
        User u = getUser(id);
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            String email = req.getEmail().trim();
            if (!email.equalsIgnoreCase(u.getEmail()) && userRepo.existsByEmail(email))
                throw new IllegalArgumentException("Email already exists");
            u.setEmail(email);
        }
        if (req.getName() != null) u.setName(req.getName().trim());
        if (req.getPhone() != null) u.setPhone(req.getPhone().isBlank() ? null : req.getPhone().trim());
        return userRepo.save(u);
    }

    // ─── Admin management (SUPER_ADMIN only) ──────────────────────────────────

    public List<Admin> getAllAdmins() { return adminRepo.findAll(); }

    public void toggleAdmin(String id, boolean active) {
        Admin a = adminRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        a.setActive(active);
        adminRepo.save(a);
    }

    public void deleteAdmin(String id) { adminRepo.deleteById(id); }
}
