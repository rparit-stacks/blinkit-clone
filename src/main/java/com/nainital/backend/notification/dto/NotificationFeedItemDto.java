package com.nainital.backend.notification.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public record NotificationFeedItemDto(
        String id,
        String title,
        String message,
        boolean read,
        String createdAt,
        String time,
        String type,
        String relatedEntityKind,
        String relatedEntityId
) {
    public static NotificationFeedItemDto fromEvent(
            String id,
            String createdAtIso,
            String title,
            String body,
            String type,
            String relatedKind,
            String relatedId
    ) {
        return new NotificationFeedItemDto(
                id,
                title,
                body,
                false,
                createdAtIso != null ? createdAtIso : Instant.now().toString(),
                formatRelativeTime(createdAtIso),
                type,
                relatedKind,
                relatedId
        );
    }

    private static String formatRelativeTime(String iso) {
        if (iso == null || iso.isBlank()) return "Just now";
        try {
            Instant at = Instant.parse(iso);
            long mins = ChronoUnit.MINUTES.between(at, Instant.now());
            if (mins < 1) return "Just now";
            if (mins < 60) return mins + " min ago";
            long hours = ChronoUnit.HOURS.between(at, Instant.now());
            if (hours < 24) return hours + " hr ago";
            return DateTimeFormatter.ofPattern("dd MMM")
                    .withZone(ZoneId.systemDefault())
                    .format(at);
        } catch (Exception e) {
            return "Recently";
        }
    }
}
