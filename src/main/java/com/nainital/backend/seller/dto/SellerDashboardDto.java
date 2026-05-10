package com.nainital.backend.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardDto {
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long totalRevenue;
    private long todayOrders;
    private long todayRevenue;
    private long totalProducts;
    private long activeProducts;
}
