package com.nainital.backend.internal;

import com.nainital.backend.common.ApiResponse;
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

    public record TokenHolder(String userId, List<String> tokens) {}

    @GetMapping("/users/{userId}/tokens")
    public ApiResponse<List<String>> userTokens(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(u -> ApiResponse.ok(nonEmptyTokens(u)))
                .orElseGet(() -> ApiResponse.ok(List.of()));
    }

  /** Users with at least one FCM token for role-based broadcast push. */
    @GetMapping("/roles/{role}/token-holders")
    public ApiResponse<List<TokenHolder>> roleTokenHolders(@PathVariable String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase();
        List<TokenHolder> holders = new ArrayList<>();
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
}
