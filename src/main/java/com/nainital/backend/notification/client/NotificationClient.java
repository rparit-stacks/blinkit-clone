package com.nainital.backend.notification.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nainital.backend.notification.config.NotificationProperties;
import com.nainital.backend.notification.dto.NotificationFeedItemDto;
import com.nainital.backend.notification.dto.NotificationRecipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NotificationClient {

    private final RestClient restClient;
    private final NotificationProperties props;

    public NotificationClient(RestClient notificationRestClient, NotificationProperties props) {
        this.restClient = notificationRestClient;
        this.props = props;
    }

    public void submit(
            String sourceModule,
            String actorId,
            String actorType,
            List<NotificationRecipient> recipients,
            List<String> broadcastRoles,
            String type,
            String title,
            String body,
            String priority,
            String relatedKind,
            String relatedId,
            String idempotencyKey
    ) {
        if (!props.isConfigured()) {
            log.debug("Notification service not configured — skipping: {}", title);
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("module", sourceModule);
        if (actorId != null) source.put("actorId", actorId);
        if (actorType != null) source.put("actorType", actorType);
        payload.put("source", source);

        Map<String, Object> targets = new LinkedHashMap<>();
        if (recipients != null && !recipients.isEmpty()) {
            List<Map<String, String>> recs = new ArrayList<>();
            for (NotificationRecipient r : recipients) {
                recs.add(Map.of("userId", r.userId(), "role", r.role()));
            }
            targets.put("recipients", recs);
        }
        if (broadcastRoles != null && !broadcastRoles.isEmpty()) {
            targets.put("broadcastRoles", broadcastRoles);
        }
        payload.put("targets", targets);

        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("type", type);
        notification.put("title", title);
        notification.put("body", body);
        notification.put("priority", priority != null ? priority : "NORMAL");
        if (relatedKind != null && relatedId != null) {
            notification.put("relatedEntity", Map.of("kind", relatedKind, "id", relatedId));
        }
        payload.put("notification", notification);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            payload.put("idempotencyKey", idempotencyKey);
        }

        try {
            restClient.post()
                    .uri("/v1/notifications")
                    .header("X-Api-Key", props.getApiKey())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to submit notification '{}': {}", title, e.getMessage());
        }
    }

    public List<NotificationFeedItemDto> fetchFeed(String userId, String role, int limit) {
        if (!props.isConfigured()) {
            return List.of();
        }
        try {
            FeedApiResponse res = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/feed")
                            .queryParam("userId", userId)
                            .queryParam("role", role)
                            .queryParam("limit", limit)
                            .build())
                    .header("X-Api-Key", props.getApiKey())
                    .retrieve()
                    .body(FeedApiResponse.class);
            if (res == null || res.data == null || res.data.items == null) {
                return List.of();
            }
            return res.data.items.stream()
                    .filter(i -> i.event != null)
                    .map(i -> NotificationFeedItemDto.fromEvent(
                            i.event.id,
                            i.event.createdAt,
                            i.event.title,
                            i.event.body,
                            i.event.notificationType,
                            i.event.relatedEntityKind,
                            i.event.relatedEntityId))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to fetch notification feed for {} / {}: {}", userId, role, e.getMessage());
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeedApiResponse {
        public boolean success;
        public FeedData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeedData {
        public List<FeedItem> items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeedItem {
        public String channel;
        public String deliveryId;
        public String broadcastId;
        public FeedEvent event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeedEvent {
        public String id;
        public String createdAt;
        public String title;
        public String body;
        public String notificationType;
        public String priority;
        public String relatedEntityKind;
        public String relatedEntityId;
    }
}
