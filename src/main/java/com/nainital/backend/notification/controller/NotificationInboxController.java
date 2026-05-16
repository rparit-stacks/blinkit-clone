package com.nainital.backend.notification.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.notification.dto.NotificationFeedItemDto;
import com.nainital.backend.notification.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NotificationInboxController {

    private final NotificationInboxService inboxService;

    @GetMapping("/api/notifications")
    public ResponseEntity<ApiResponse<Map<String, List<NotificationFeedItemDto>>>> customerFeed(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "30") int limit) {
        var items = inboxService.getFeed(user.getUsername(), "CUSTOMER", clamp(limit));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("items", items)));
    }

    @GetMapping("/api/seller/notifications")
    public ResponseEntity<ApiResponse<Map<String, List<NotificationFeedItemDto>>>> sellerFeed(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "30") int limit) {
        var items = inboxService.getFeed(user.getUsername(), "SELLER", clamp(limit));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("items", items)));
    }

    @GetMapping("/api/delivery/notifications")
    public ResponseEntity<ApiResponse<Map<String, List<NotificationFeedItemDto>>>> deliveryFeed(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "30") int limit) {
        var items = inboxService.getFeed(user.getUsername(), "DELIVERY", clamp(limit));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("items", items)));
    }

    @GetMapping("/api/admin/notifications")
    public ResponseEntity<ApiResponse<Map<String, List<NotificationFeedItemDto>>>> adminFeed(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "30") int limit) {
        var items = inboxService.getFeed(user.getUsername(), "ADMIN", clamp(limit));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("items", items)));
    }

    private static int clamp(int limit) {
        return Math.max(1, Math.min(limit, 100));
    }
}
