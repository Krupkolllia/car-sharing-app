package org.project.carsharingapp.service;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.properties.TelegramProperties;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

public class TelegramNotificationServiceTest {

    private static final String API_URL = "https://api.telegram.org";
    private static final String BOT_TOKEN = "test-token";
    private static final String CHAT_ID = "1234567890";

    private MockRestServiceServer mockServer;

    private TelegramNotificationService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
            .baseUrl(API_URL);

        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        TelegramProperties properties = new TelegramProperties(
            BOT_TOKEN,
            CHAT_ID,
            API_URL
        );

        service = new TelegramNotificationService(properties, restClient);
    }

    @Test
    @DisplayName("""
        sendNotification method with valid message should
        send post request to Telegram
        """)
    void sendNotification_WithValidMessage_ShouldSendPostRequestToTelegram() {
        // Given
        String message = "test";

        mockServer.expect(once(), requestTo(containsString(
            "/bot" + BOT_TOKEN + "/sendMessage")))
            .andExpect(method(POST))
            .andExpect(queryParam("chat_id", CHAT_ID))
            .andExpect(queryParam("text", message))
            .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        // When
        service.sendNotification(message);

        // Then
        mockServer.verify();
    }

    @Test
    @DisplayName("""
        sendNotification method when Telegram returns error
        should not throw exception
        """)
    void sendNotification_WhenTelegramReturnsError_ShouldNotThrowException() {
        // Given
        String message = "test";

        mockServer.expect(once(), requestTo(containsString(
            "/bot" + BOT_TOKEN + "/sendMessage")))
            .andExpect(method(POST))
            .andExpect(queryParam("chat_id", CHAT_ID))
            .andExpect(queryParam("text", message))
            .andRespond(withServerError());

        // When & Then
        assertDoesNotThrow(() -> service.sendNotification(message));

        mockServer.verify();
    }
}
