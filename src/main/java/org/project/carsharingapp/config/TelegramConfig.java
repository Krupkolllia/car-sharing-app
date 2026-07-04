package org.project.carsharingapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class TelegramConfig {

    private final TelegramProperties properties;

    @Bean
    public RestClient telegramRestClient() {
        return RestClient.builder()
            .baseUrl(properties.apiUrl())
            .build();
    }
}
