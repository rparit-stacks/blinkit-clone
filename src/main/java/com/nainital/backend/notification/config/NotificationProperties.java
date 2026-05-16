package com.nainital.backend.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.service")
public class NotificationProperties {

    /** Base URL e.g. https://nainital-notification-queue.vercel.app */
    private String url = "";

    private String apiKey = "";

    private boolean enabled = true;

    public boolean isConfigured() {
        return enabled && url != null && !url.isBlank() && apiKey != null && !apiKey.isBlank();
    }
}
