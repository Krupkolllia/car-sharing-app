package org.project.carsharingapp.service.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.exception.StripeSessionCreationException;
import org.project.carsharingapp.exception.StripeSessionRetrievingException;
import org.project.carsharingapp.properties.PaymentProperties;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {

    private static final String STRIPE_PAID_STATUS = "paid";

    private static final String STRIPE_EXPIRED_STATUS = "expired";

    private static final BigDecimal CENTS_IN_DOLLAR = BigDecimal.valueOf(100);

    private final StripePaymentUrlBuilder urlBuilder;

    private final PaymentProperties paymentProperties;

    @Override
    public PaymentSession createSession(PaymentSessionRequest request) {
        Stripe.apiKey = paymentProperties.stripe().secretKey();

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
            Session session = Session.create(params);

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
    public PaymentSessionStatus getStatus(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

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
