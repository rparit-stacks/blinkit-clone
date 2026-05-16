package com.nainital.backend.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "internal.api")
public class InternalApiProperties {

    private String key = "";

    public boolean isConfigured() {
        return key != null && !key.isBlank();
    }

    public String getApiKey() {
        return key == null ? "" : key.trim();
    }
}
