package org.project.carsharingapp.service.payment;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import com.stripe.param.checkout.SessionExpireParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.exception.StripeSessionCreationException;
import org.project.carsharingapp.exception.StripeSessionExpirationException;
import org.project.carsharingapp.exception.StripeSessionRetrievingException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {

    private static final String STRIPE_PAID_STATUS = "paid";

    private static final String STRIPE_EXPIRED_STATUS = "expired";

    private static final BigDecimal CENTS_IN_DOLLAR = BigDecimal.valueOf(100);

    private final StripeClient stripeClient;

    private final StripePaymentUrlBuilder urlBuilder;

    @Override
    public PaymentSession createSession(PaymentSessionRequest request) {
        long unitAmount = request.amount()
                .multiply(CENTS_IN_DOLLAR)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(Mode.PAYMENT)
                .setSuccessUrl(urlBuilder.buildSuccessUrl())
                .setCancelUrl(urlBuilder.buildCancelUrl())
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(request.quantity())
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(request.currency())
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(request.productName())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                ).build();

        try {
            Session session = stripeClient
                    .v1()
                    .checkout()
                    .sessions()
                    .create(params);

            return new PaymentSession(
                session.getId(),
                session.getUrl()
            );
        } catch (StripeException e) {
            log.error("Failed to create Stripe session", e);
            throw new StripeSessionCreationException("Cannot create Stripe session", e);
        }
    }

    @Override
    public void expireSession(String sessionId) {
        try {
            SessionExpireParams params = SessionExpireParams.builder()
                    .build();

            stripeClient
                    .v1()
                    .checkout()
                    .sessions()
                    .expire(sessionId, params);
        } catch (StripeException e) {
            throw new StripeSessionExpirationException(
                "Failed to expire Stripe session with id: " + sessionId,
                e
            );
        }
    }

    @Override
    public PaymentSessionStatus getStatus(String sessionId) {
        try {
            Session session = stripeClient
                    .v1()
                    .checkout()
                    .sessions()
                    .retrieve(sessionId);

            if (STRIPE_PAID_STATUS.equals(session.getPaymentStatus())) {
                return PaymentSessionStatus.PAID;
            }

            if (STRIPE_EXPIRED_STATUS.equals(session.getStatus())) {
                return PaymentSessionStatus.EXPIRED;
            }

            return PaymentSessionStatus.UNPAID;
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe session", e);
            throw new StripeSessionRetrievingException("Cannot retrieve Stripe session", e);
        }
    }
}
