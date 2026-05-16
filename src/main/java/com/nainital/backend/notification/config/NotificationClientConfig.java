package com.nainital.backend.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationClientConfig {

    @Bean
    RestClient notificationRestClient(NotificationProperties props) {
        String base = props.getUrl() == null ? "" : props.getUrl().trim().replaceAll("/+$", "");
        return RestClient.builder()
                .baseUrl(base.isEmpty() ? "http://localhost:4005" : base)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "notificationExecutor")
    Executor notificationExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
