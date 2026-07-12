package org.project.carsharingapp.service.payment;

import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.properties.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class StripePaymentUrlBuilder {

    private static final String SUCCESS_PATH = "/payments/success";
    private static final String CANCEL_PATH = "/payments/cancel";
    private static final String SESSION_ID_PARAM = "session_id";
    private static final String CHECKOUT_SESSION_ID = "{CHECKOUT_SESSION_ID}";

    private final AppProperties appProperties;

    public String buildSuccessUrl() {
        return UriComponentsBuilder
            .fromUriString(appProperties.baseUrl())
            .path(SUCCESS_PATH)
            .queryParam(SESSION_ID_PARAM, CHECKOUT_SESSION_ID)
            .build(false)
            .toUriString();
    }

    public String buildCancelUrl() {
        return UriComponentsBuilder
            .fromUriString(appProperties.baseUrl())
            .path(CANCEL_PATH)
            .build()
            .toUriString();
    }

}
