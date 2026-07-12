package org.project.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.properties.TelegramProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
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
        } catch (RestClientResponseException e) {
            System.err.println("Failed to send Telegram notification. Status: "
                    + e.getStatusCode() + ", body: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            System.err.println("Failed to send Telegram notification: " + e.getMessage());
        }
    }
}
