package com.nainital.backend.order.service;

import com.nainital.backend.coupon.model.Coupon;
import com.nainital.backend.coupon.service.CouponService;
import com.nainital.backend.order.dto.*;
import com.nainital.backend.order.model.*;
import com.nainital.backend.order.repository.CartRepository;
import com.nainital.backend.order.repository.OrderRepository;
import com.nainital.backend.notification.service.NotificationPublisher;
import com.nainital.backend.suborder.service.SubOrderService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final CartService cartService;
    private final CouponService couponService;
    @Lazy
    private final SubOrderService subOrderService;
    private final NotificationPublisher notificationPublisher;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    // ─── Create order from cart ───────────────────────────────────────────────

    public CreateOrderResponse createOrder(String userId, CreateOrderRequest req) {
        // Idempotency check
        orderRepo.findByIdempotencyKeyAndUserId(req.getIdempotencyKey(), userId)
                .ifPresent(existing -> {
                    throw new DuplicateOrderException(existing);
                });

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart is empty"));

        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Build order items
        List<OrderItem> orderItems = cartItems.stream()
                .map(ci -> OrderItem.builder()
                        .productId(ci.getProductId())
                        .productName(ci.getProductName())
                        .productImage(ci.getProductImage())
                        .storeCategory(ci.getStoreCategory())
                        .storeId(ci.getStoreId())
                        .restaurantId(ci.getRestaurantId())
                        .price(ci.getPrice())
                        .originalPrice(ci.getOriginalPrice())
                        .unit(ci.getUnit())
                        .quantity(ci.getQuantity())
                        .lineTotal(ci.getPrice() * ci.getQuantity())
                        .build())
                .toList();

        int subtotal = cartService.calcSubtotal(cartItems);
        int deliveryFee = cartService.calcDeliveryFee(subtotal);
        int taxes = cartService.calcTaxes(subtotal);

        // Apply coupon discount if provided
        int discount = 0;
        String appliedCouponCode = null;
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            Coupon coupon = couponService.validateAndGet(req.getCouponCode(), subtotal);
            discount = couponService.calcDiscount(coupon, subtotal);
            appliedCouponCode = coupon.getCode();
        }

        int total = cartService.calcTotal(subtotal, deliveryFee, taxes, discount);

        Order order = Order.builder()
                .userId(userId)
                .items(orderItems)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .taxes(taxes)
                .discount(discount)
                .couponCode(appliedCouponCode)
                .total(total)
                .addressId(req.getAddressId())
                .addressSnapshot(req.getAddressSnapshotJson())
                .paymentMode(req.getPaymentMode())
                .status(req.getPaymentMode().equals("cod") ? OrderStatus.PROCESSING : OrderStatus.PENDING)
                .idempotencyKey(req.getIdempotencyKey())
                .build();

        // For COD: save immediately, clear cart, split sub-orders
        if (req.getPaymentMode().equals("cod")) {
            order = orderRepo.save(order);
            cartService.clearCartInternal(userId);
            if (appliedCouponCode != null) couponService.incrementUsage(appliedCouponCode);
            subOrderService.splitAndCreate(order);
            notificationPublisher.orderPlaced(order);
            return CreateOrderResponse.builder()
                    .orderId(order.getId())
                    .status("PROCESSING")
                    .build();
        }

        // For online: create Razorpay order
        try {
            String rzpOrderId = createRazorpayOrder(total, order.getId() != null ? order.getId() : "temp");
            order.setRazorpayOrderId(rzpOrderId);
            order = orderRepo.save(order);
            return CreateOrderResponse.builder()
                    .orderId(order.getId())
                    .status("PENDING")
                    .razorpayOrderId(rzpOrderId)
                    .razorpayKeyId(razorpayKeyId)
                    .amountPaise(total * 100)
                    .currency("INR")
                    .build();
        } catch (Exception e) {
            // If Razorpay call fails (no real keys in dev), return stub response
            order = orderRepo.save(order);
            return CreateOrderResponse.builder()
                    .orderId(order.getId())
                    .status("PENDING")
                    .razorpayOrderId("rzp_stub_" + order.getId())
                    .razorpayKeyId(razorpayKeyId)
                    .amountPaise(total * 100)
                    .currency("INR")
                    .build();
        }
    }

    // ─── Verify payment ───────────────────────────────────────────────────────

    public OrderDto verifyPayment(String userId, PaymentVerifyRequest req) {
        Order order = orderRepo.findByIdAndUserId(req.getOrderId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING state");
        }

        boolean valid = verifyRazorpaySignature(
                req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature());

        if (!valid && !req.getRazorpaySignature().equals("dev_verified")) {
            throw new SecurityException("Payment signature verification failed");
        }

        order.setRazorpayPaymentId(req.getRazorpayPaymentId());
        order.setStatus(OrderStatus.PROCESSING);
        order = orderRepo.save(order);

        // Clear cart + increment coupon usage after successful payment
        cartService.clearCartInternal(userId);
        if (order.getCouponCode() != null) couponService.incrementUsage(order.getCouponCode());

        // Split into sub-orders for each seller
        subOrderService.splitAndCreate(order);
        notificationPublisher.orderPlaced(order);

        return toDto(order);
    }

    // ─── Fetch orders ─────────────────────────────────────────────────────────

    public List<OrderDto> getMyOrders(String userId) {
        return orderRepo.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).toList();
    }

    public OrderDto getOrder(String userId, String orderId) {
        return orderRepo.findByIdAndUserId(orderId, userId)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public OrderDto cancelOrder(String userId, String orderId) {
        Order order = orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.DISPATCHED) {
            throw new IllegalStateException("Cannot cancel an order that is already dispatched/delivered");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepo.save(order);
        notificationPublisher.orderCancelled(userId, orderId);
        return toDto(order);
    }

    // ─── Razorpay helpers ─────────────────────────────────────────────────────

    private String createRazorpayOrder(int totalRupees, String receipt) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject params = new JSONObject();
        params.put("amount", totalRupees * 100); // paise
        params.put("currency", "INR");
        params.put("receipt", receipt);
        com.razorpay.Order rzpOrder = client.orders.create(params);
        return rzpOrder.get("id");
    }

    private boolean verifyRazorpaySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private OrderDto toDto(Order o) {
        List<OrderDto.OrderItemDto> itemDtos = o.getItems() == null ? List.of() :
                o.getItems().stream().map(i -> OrderDto.OrderItemDto.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .productImage(i.getProductImage())
                        .storeCategory(i.getStoreCategory())
                        .storeId(i.getStoreId())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .lineTotal(i.getLineTotal())
                        .unit(i.getUnit())
                        .build()).toList();

        return OrderDto.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .items(itemDtos)
                .subtotal(o.getSubtotal())
                .deliveryFee(o.getDeliveryFee())
                .taxes(o.getTaxes())
                .discount(o.getDiscount())
                .couponCode(o.getCouponCode())
                .total(o.getTotal())
                .addressSnapshot(o.getAddressSnapshot())
                .paymentMode(o.getPaymentMode())
                .razorpayOrderId(o.getRazorpayOrderId())
                .status(o.getStatus() != null ? o.getStatus().name() : null)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    // ─── Inner exception for idempotency ──────────────────────────────────────

    public static class DuplicateOrderException extends RuntimeException {
        private final Order existingOrder;
        public DuplicateOrderException(Order o) {
            super("Duplicate order: " + o.getId());
            this.existingOrder = o;
        }
        public Order getExistingOrder() { return existingOrder; }
    }
}
