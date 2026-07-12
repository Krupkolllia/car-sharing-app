package org.project.carsharingapp.service.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.carsharingapp.properties.TelegramProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service

@Slf4j
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private final TelegramProperties properties;

    private final RestClient telegramRestClient;

    @Override
    public void sendNotification(String message) {
        try {
            telegramRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                        .path("/bot{token}/sendMessage")
                        .queryParam("chat_id", properties.chatId())
                        .queryParam("text", message)
                        .build(properties.botToken()))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Telegram notification sent: chatId={}", properties.chatId());
        } catch (RestClientResponseException e) {
            log.error("Failed to send Telegram notification. Status: {}, response body: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {
            log.error("Failed to send Telegram notification", e);
        }
    }
}
