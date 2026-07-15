package org.project.carsharingapp.service.payment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.properties.AppProperties;

class StripePaymentUrlBuilderTest {

    private static final String SUCCESS_PATH = "/payments/success";
    private static final String CANCEL_PATH = "/payments/cancel";
    private static final String SESSION_ID_PARAM = "session_id";
    private static final String CHECKOUT_SESSION_ID = "{CHECKOUT_SESSION_ID}";

    private AppProperties appProperties;

    private StripePaymentUrlBuilder urlBuilder;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties("http://localhost:8080/api");
        urlBuilder = new StripePaymentUrlBuilder(appProperties);
    }

    @Test
    @DisplayName("""
        buildSuccessUrl method always should
        return correct success URL with Stripe session id
        """)
    void buildSuccessUrl_ShouldReturnCorrectSuccessUrl() {
        // Given
        String expected = appProperties.baseUrl()
            + SUCCESS_PATH
            + "?"
            + SESSION_ID_PARAM
            + "="
            + CHECKOUT_SESSION_ID;

        // When
        String actual = urlBuilder.buildSuccessUrl();

        // Then
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    @DisplayName("""
        buildCancelUrl method always should
        return correct cancel URL
        """)
    void buildCancelUrl_ShouldReturnCorrectCancelUrl() {
        // Given
        String expected = appProperties.baseUrl() + CANCEL_PATH;

        // When
        String actual = urlBuilder.buildCancelUrl();

        // Then
        assertThat(actual).isEqualTo(expected);

    }

}
