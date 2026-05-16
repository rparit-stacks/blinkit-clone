package com.nainital.backend.internal;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.repository.DeliveryPartnerRepository;
import com.nainital.backend.user.model.User;
import com.nainital.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/internal/push")
@RequiredArgsConstructor
public class InternalPushController {

    private final UserRepository userRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    public record TokenHolder(String userId, List<String> tokens) {}

    @GetMapping("/users/{userId}/tokens")
    public ApiResponse<List<String>> userTokens(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(u -> ApiResponse.ok(nonEmptyTokens(u)))
                .orElseGet(() -> deliveryPartnerRepository.findById(userId)
                        .map(p -> ApiResponse.ok(nonEmptyPartnerTokens(p)))
                        .orElseGet(() -> ApiResponse.ok(List.of())));
    }

    /** Users/partners with at least one FCM token for role-based broadcast push. */
    @GetMapping("/roles/{role}/token-holders")
    public ApiResponse<List<TokenHolder>> roleTokenHolders(@PathVariable String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase();
        List<TokenHolder> holders = new ArrayList<>();

        if ("DELIVERY".equals(normalized)) {
            for (DeliveryPartner partner : deliveryPartnerRepository.findAll()) {
                List<String> tokens = nonEmptyPartnerTokens(partner);
                if (!tokens.isEmpty()) {
                    holders.add(new TokenHolder(partner.getId(), tokens));
                }
            }
            return ApiResponse.ok(holders);
        }

        for (User user : userRepository.findByRole(normalized)) {
            List<String> tokens = nonEmptyTokens(user);
            if (!tokens.isEmpty()) {
                holders.add(new TokenHolder(user.getId(), tokens));
            }
        }
        return ApiResponse.ok(holders);
    }

    private static List<String> nonEmptyTokens(User user) {
        if (user.getFcmTokens() == null) return List.of();
        return user.getFcmTokens().stream()
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .toList();
    }

    private static List<String> nonEmptyPartnerTokens(DeliveryPartner partner) {
        if (partner.getFcmTokens() == null) return List.of();
        return partner.getFcmTokens().stream()
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .toList();
    }
}
