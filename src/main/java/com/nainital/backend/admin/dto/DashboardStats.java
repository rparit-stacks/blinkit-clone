package com.nainital.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DashboardStats {
    private long totalUsers;
    private long totalOrders;
    private long pendingOrders;
    private long processingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long totalProducts;
    private long totalStores;
    private long totalDeliveryPartners;
    private long totalCoupons;
    private long totalRevenue;
    private long todayOrders;
    private long todayRevenue;
}
