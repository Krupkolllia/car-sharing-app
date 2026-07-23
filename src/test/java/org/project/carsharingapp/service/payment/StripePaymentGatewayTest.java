package org.project.carsharingapp.service.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.createPaymentSessionRequest;

import com.stripe.StripeClient;
import com.stripe.exception.ApiConnectionException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionExpireParams;
import com.stripe.service.CheckoutService;
import com.stripe.service.V1Services;
import com.stripe.service.checkout.SessionService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.exception.StripeSessionCreationException;
import org.project.carsharingapp.exception.StripeSessionExpirationException;
import org.project.carsharingapp.exception.StripeSessionRetrievingException;

@ExtendWith(MockitoExtension.class)
class StripePaymentGatewayTest {

    private static final String SESSION_ID = "cs-test-session-id";

    private static final String SESSION_URL =
        "https://checkout.stripe.com/c/pay/" + SESSION_ID;

    private static final String SUCCESS_URL =
        "http://localhost:8080/api/payments/success?session_id={CHECKOUT_SESSION_ID}";

    private static final String CANCEL_URL =
        "http://localhost:8080/api/payments/cancel";

    private static final String STRIPE_SESSION_STATUS_OPEN = "open";

    private static final String STRIPE_SESSION_STATUS_COMPLETE = "complete";

    private static final String STRIPE_SESSION_STATUS_EXPIRED = "expired";

    private static final String STRIPE_PAYMENT_STATUS_PAID = "paid";

    private static final String STRIPE_PAYMENT_STATUS_UNPAID = "unpaid";

    @Mock
    private StripeClient stripeClient;

    @Mock
    private V1Services v1Services;

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private SessionService sessionService;

    @Mock
    private StripePaymentUrlBuilder urlBuilder;

    private StripePaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        when(stripeClient.v1()).thenReturn(v1Services);
        when(v1Services.checkout()).thenReturn(checkoutService);
        when(checkoutService.sessions()).thenReturn(sessionService);

        paymentGateway = new StripePaymentGateway(
            stripeClient,
            urlBuilder
        );
    }

    @Test
    @DisplayName("""
        createSession method in a valid case should
        return payment session
        """)
    void createSession_ValidCase_ShouldReturnPaymentSession() throws Exception {
        // Given
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest();

        Session stripeSession = new Session();
        stripeSession.setId(SESSION_ID);
        stripeSession.setUrl(SESSION_URL);

        PaymentSession expected = new PaymentSession(SESSION_ID, SESSION_URL);

        when(urlBuilder.buildSuccessUrl()).thenReturn(SUCCESS_URL);
        when(urlBuilder.buildCancelUrl()).thenReturn(CANCEL_URL);

        when(sessionService.create(any(SessionCreateParams.class)))
            .thenReturn(stripeSession);

        // When
        PaymentSession actual = paymentGateway.createSession(paymentSessionRequest);

        // Then
        assertThat(actual).isEqualTo(expected);

        verify(urlBuilder).buildSuccessUrl();
        verify(urlBuilder).buildCancelUrl();

        verify(sessionService).create(any(SessionCreateParams.class));

        verifyNoMoreInteractions(urlBuilder, sessionService);

    }

    @Test
    @DisplayName("""
        createSession method in a valid case should
        build correct Stripe params
        """)
    void createSession_ValidCase_ShouldBuildCorrectStripeParams() throws Exception {
        // Given
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest();

        Session stripeSession = new Session();
        stripeSession.setId(SESSION_ID);
        stripeSession.setUrl(SESSION_URL);

        when(urlBuilder.buildSuccessUrl()).thenReturn(SUCCESS_URL);
        when(urlBuilder.buildCancelUrl()).thenReturn(CANCEL_URL);

        when(sessionService.create(any(SessionCreateParams.class)))
            .thenReturn(stripeSession);

        ArgumentCaptor<SessionCreateParams> paramsCaptor =
            ArgumentCaptor.forClass(SessionCreateParams.class);

        // When
        paymentGateway.createSession(paymentSessionRequest);

        // Then
        verify(sessionService).create(paramsCaptor.capture());

        SessionCreateParams params = paramsCaptor.getValue();

        assertThat(params.getMode()).isSameAs(SessionCreateParams.Mode.PAYMENT);

        assertThat(params.getSuccessUrl()).isEqualTo(SUCCESS_URL);
        assertThat(params.getCancelUrl()).isEqualTo(CANCEL_URL);

        assertThat(params.getLineItems()).hasSize(1);

        SessionCreateParams.LineItem lineItem =
            params.getLineItems().get(0);

        assertThat(lineItem.getQuantity()).isEqualTo(paymentSessionRequest.quantity());

        SessionCreateParams.LineItem.PriceData priceData =
            lineItem.getPriceData();

        assertThat(priceData.getCurrency()).isEqualTo(paymentSessionRequest.currency());

        assertThat(priceData.getUnitAmount()).isEqualTo(12346L);

        assertThat(priceData.getProductData().getName())
            .isEqualTo(paymentSessionRequest.productName());

        verify(urlBuilder).buildSuccessUrl();
        verify(urlBuilder).buildCancelUrl();

        verifyNoMoreInteractions(urlBuilder, sessionService);

    }

    @ParameterizedTest
    @CsvSource({
        "123.444, 12344",
        "234.153, 23415",
        "5473.932, 547393",
        "1.111, 111",
        "34.450, 3445",
        "999.99, 99999"
    })
    @DisplayName("""
        createSession method when third decimal is below five
        should round down
        """)
    void createSession_WhenThirdDecimalIsBelowFive_ShouldRoundDown(
            String amount, long expectedUnitAmount) throws Exception {
        // Given
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
            .withAmount(new BigDecimal(amount));

        Session stripeSession = new Session();
        stripeSession.setId(SESSION_ID);
        stripeSession.setUrl(SESSION_URL);

        when(urlBuilder.buildSuccessUrl()).thenReturn(SUCCESS_URL);
        when(urlBuilder.buildCancelUrl()).thenReturn(CANCEL_URL);

        when(sessionService.create(any(SessionCreateParams.class)))
            .thenReturn(stripeSession);

        ArgumentCaptor<SessionCreateParams> paramsCaptor =
            ArgumentCaptor.forClass(SessionCreateParams.class);

        // When
        paymentGateway.createSession(paymentSessionRequest);

        // Then
        verify(sessionService).create(paramsCaptor.capture());

        long unitAmount = paramsCaptor.getValue()
            .getLineItems()
            .get(0)
            .getPriceData()
            .getUnitAmount();

        assertThat(unitAmount).isEqualTo(expectedUnitAmount);

        verify(urlBuilder).buildSuccessUrl();
        verify(urlBuilder).buildCancelUrl();

        verifyNoMoreInteractions(urlBuilder, sessionService);

    }

    @ParameterizedTest
    @CsvSource({
        "123.445, 12345",
        "234.155, 23416",
        "5473.936, 547394",
        "1.115, 112",
        "34.456, 3446",
        "999.999, 100000"
    })
    @DisplayName("""
        createSession method when third decimal is five or above
        should round up
    """)
    void createSession_WhenThirdDecimalIsFiveOrAbove_ShouldRoundUp(
        String amount, long expectedUnitAmount) throws Exception {
        // Given
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
            .withAmount(new BigDecimal(amount));

        Session stripeSession = new Session();
        stripeSession.setId(SESSION_ID);
        stripeSession.setUrl(SESSION_URL);

        when(urlBuilder.buildSuccessUrl()).thenReturn(SUCCESS_URL);
        when(urlBuilder.buildCancelUrl()).thenReturn(CANCEL_URL);

        when(sessionService.create(any(SessionCreateParams.class)))
            .thenReturn(stripeSession);

        ArgumentCaptor<SessionCreateParams> paramsCaptor =
            ArgumentCaptor.forClass(SessionCreateParams.class);

        // When
        paymentGateway.createSession(paymentSessionRequest);

        // Then
        verify(sessionService).create(paramsCaptor.capture());

        long unitAmount = paramsCaptor.getValue()
            .getLineItems()
            .get(0)
            .getPriceData()
            .getUnitAmount();

        assertThat(unitAmount).isEqualTo(expectedUnitAmount);

        verify(urlBuilder).buildSuccessUrl();
        verify(urlBuilder).buildCancelUrl();

        verifyNoMoreInteractions(urlBuilder, sessionService);
    }

    @Test
    @DisplayName("""
        createSession method when Stripe throws exception should
        throw StripeSessionCreationException
        """)
    void createSession_WhenStripeThrowsException_ShouldThrowStripeSessionCreationException()
        throws Exception {
        // Given
        PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest();

        ApiConnectionException stripeException = new ApiConnectionException("Stripe is unavailable");

        when(urlBuilder.buildSuccessUrl()).thenReturn(SUCCESS_URL);
        when(urlBuilder.buildCancelUrl()).thenReturn(CANCEL_URL);

        when(sessionService.create(any(SessionCreateParams.class)))
            .thenThrow(stripeException);

        // When & Then
        assertThatThrownBy(() -> paymentGateway.createSession(paymentSessionRequest))
            .isExactlyInstanceOf(StripeSessionCreationException.class)
            .hasMessage("Cannot create Stripe session")
            .hasCauseExactlyInstanceOf(stripeException.getClass());

        verify(sessionService).create(any(SessionCreateParams.class));

        verify(urlBuilder).buildSuccessUrl();
        verify(urlBuilder).buildCancelUrl();

        verifyNoMoreInteractions(urlBuilder, sessionService);
    }

    @Test
    @DisplayName("""
        expireSession method with valid session id should
        call Stripe
        """)
    void expireSession_WithValidSessionId_ShouldCallStripe() throws Exception {
        // When & Then
        paymentGateway.expireSession(SESSION_ID);

        verify(sessionService).expire(eq(SESSION_ID), any(SessionExpireParams.class));

    }

    @Test
    @DisplayName("""
        expireSession method when Stripe throws exception should
        throw StripeSessionExpirationException
        """)
    void expireSession_WhenStripeThrowsException_ShouldThrowStripeSessionExpirationException()
        throws Exception {
        // Given
        ApiConnectionException stripeException =
                new ApiConnectionException("Stripe is unavailable");

        when(sessionService.expire(eq(SESSION_ID), any(SessionExpireParams.class)))
            .thenThrow(stripeException);

        // When & Then
        assertThatThrownBy(() -> paymentGateway.expireSession(SESSION_ID))
            .isExactlyInstanceOf(StripeSessionExpirationException.class)
            .hasMessage("Failed to expire Stripe session with id: " + SESSION_ID)
            .hasCauseExactlyInstanceOf(stripeException.getClass());

        verify(sessionService).expire(eq(SESSION_ID), any(SessionExpireParams.class));

        verifyNoMoreInteractions(sessionService);

    }

    @Test
    @DisplayName("""
        getStatus method when session status is complete
        and payment status is paid should
        return PaymentSessionStatus.PAID
        """)
    void getStatus_WhenSessionStatusIsCompleteAndPaymentStatusIsPaid_ShouldReturnPaymentSessionStatusPaid()
        throws Exception {
        // Given
        Session stripeSession = new Session();

        stripeSession.setStatus(STRIPE_SESSION_STATUS_COMPLETE);
        stripeSession.setPaymentStatus(STRIPE_PAYMENT_STATUS_PAID);

        when(sessionService.retrieve(SESSION_ID)).thenReturn(stripeSession);

        // When
        PaymentSessionStatus actual = paymentGateway.getStatus(SESSION_ID);

        // Then
        assertThat(actual).isSameAs(PaymentSessionStatus.PAID);

        verify(sessionService).retrieve(SESSION_ID);

        verifyNoMoreInteractions(sessionService);

    }

    @Test
    @DisplayName("""
        getStatus method when session status is expired
        and payment status is unpaid should
        return PaymentSessionStatus.EXPIRED
        """)
    void getStatus_WhenSessionStatusIsExpiredAndPaymentStatusIsUnpaid_ShouldReturnPaymentSessionStatusExpired()
        throws Exception {
        // Given
        Session stripeSession = new Session();

        stripeSession.setStatus(STRIPE_SESSION_STATUS_EXPIRED);
        stripeSession.setPaymentStatus(STRIPE_PAYMENT_STATUS_UNPAID);

        when(sessionService.retrieve(SESSION_ID)).thenReturn(stripeSession);

        // When
        PaymentSessionStatus actual = paymentGateway.getStatus(SESSION_ID);

        // Then
        assertThat(actual).isSameAs(PaymentSessionStatus.EXPIRED);

        verify(sessionService).retrieve(SESSION_ID);

        verifyNoMoreInteractions(sessionService);

    }

    @Test
    @DisplayName("""
        getStatus method when session status is open
        and payment status is unpaid should
        return PaymentSessionStatus.UNPAID
        """)
    void getStatus_WhenSessionStatusIsOpenAndPaymentStatusIsUnpaid_ShouldReturnPaymentSessionStatusUnpaid()
        throws Exception {
        // Given
        Session stripeSession = new Session();

        stripeSession.setStatus(STRIPE_SESSION_STATUS_OPEN);
        stripeSession.setPaymentStatus(STRIPE_PAYMENT_STATUS_UNPAID);

        when(sessionService.retrieve(SESSION_ID)).thenReturn(stripeSession);

        // When
        PaymentSessionStatus actual = paymentGateway.getStatus(SESSION_ID);

        // Then
        assertThat(actual).isSameAs(PaymentSessionStatus.UNPAID);

        verify(sessionService).retrieve(SESSION_ID);

        verifyNoMoreInteractions(sessionService);

    }

    @Test
    @DisplayName("""
        getStatus method when session status is expired
        and payment status is paid should
        return PaymentSessionStatus.PAID
        """)
    void getStatus_WhenSessionStatusIsExpiredAndPaymentStatusIsPaid_ShouldReturnPaymentSessionStatusPaid()
        throws Exception {
        // Given
        Session stripeSession = new Session();

        stripeSession.setStatus(STRIPE_SESSION_STATUS_EXPIRED);
        stripeSession.setPaymentStatus(STRIPE_PAYMENT_STATUS_PAID);

        when(sessionService.retrieve(SESSION_ID)).thenReturn(stripeSession);

        // When
        PaymentSessionStatus actual = paymentGateway.getStatus(SESSION_ID);

        // Then
        assertThat(actual).isSameAs(PaymentSessionStatus.PAID);

        verify(sessionService).retrieve(SESSION_ID);

        verifyNoMoreInteractions(sessionService);

    }

    @Test
    @DisplayName("""
        getStatus method when Stripe throws exception should
        throw StripeSessionRetrievingException
        """)
    void getStatus_WhenStripeThrowsException_ShouldThrowStripeSessionRetrievingException()
        throws Exception {
        // Given
        ApiConnectionException stripeException =
                new ApiConnectionException("Stripe is unavailable");

        when(sessionService.retrieve(SESSION_ID)).thenThrow(stripeException);

        // When & Then
        assertThatThrownBy(() -> paymentGateway.getStatus(SESSION_ID))
            .isExactlyInstanceOf(StripeSessionRetrievingException.class)
            .hasMessage("Cannot retrieve Stripe session with id: " + SESSION_ID)
            .hasCauseExactlyInstanceOf(stripeException.getClass());

        verify(sessionService).retrieve(SESSION_ID);

        verifyNoMoreInteractions(sessionService);

    }

}
