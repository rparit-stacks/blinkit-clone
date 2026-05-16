package com.nainital.backend.notification.service;

import com.nainital.backend.notification.client.NotificationClient;
import com.nainital.backend.notification.dto.NotificationFeedItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationInboxService {

    private final NotificationClient client;

    public List<NotificationFeedItemDto> getFeed(String userId, String role, int limit) {
        return client.fetchFeed(userId, role, limit);
    }
}
