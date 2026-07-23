package org.project.carsharingapp.service.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.createPaymentSessionRequest;

import com.stripe.StripeClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.properties.AppProperties;

@Tag("smoke")
@EnabledIfEnvironmentVariable(
    named = "RUN_STRIPE_SMOKE_TESTS",
    matches = "true"
)
@EnabledIfEnvironmentVariable(
    named = "STRIPE_SMOKE_TEST_KEY",
    matches = "sk_test_.+"
)
public class StripePaymentGatewaySmokeTest {

    private StripePaymentGateway paymentGateway;

    private String createdSessionId;

    private boolean sessionExpired;

    @BeforeEach
    void setUp() {
        String stripeKey = System.getenv("STRIPE_SMOKE_TEST_KEY");

        StripeClient stripeClient = new StripeClient(stripeKey);

        AppProperties appProperties = new AppProperties("http://localhost:8080/api");

        StripePaymentUrlBuilder urlBuilder = new StripePaymentUrlBuilder(appProperties);

        paymentGateway = new StripePaymentGateway(
            stripeClient,
            urlBuilder
        );
    }

    @AfterEach
    void cleanUp() {
        if (createdSessionId != null && !sessionExpired) {
            paymentGateway.expireSession(createdSessionId);
        }
    }

    @Test
    @DisplayName("""
        Stripe payment gateway should create,
        retrieve and expire a real test checkout session
        """)
    void stripePaymentGateway_ShouldCompleteCheckoutSessionLifecycle() {
        // Given
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest();

        // When
        PaymentSession paymentSession = paymentGateway.createSession(paymentSessionRequest);
        createdSessionId = paymentSession.id();

        // Then
        assertThat(paymentSession.id())
            .isNotBlank()
            .startsWith("cs_test_");

        assertThat(paymentSession.url())
            .isNotBlank()
            .startsWith("https://");

        assertThat(paymentGateway.getStatus(paymentSession.id()))
            .isEqualTo(PaymentSessionStatus.UNPAID);

        // When
        paymentGateway.expireSession(paymentSession.id());
        sessionExpired = true;

        // Then
        assertThat(paymentGateway.getStatus(paymentSession.id()))
            .isEqualTo(PaymentSessionStatus.EXPIRED);



    }

}
