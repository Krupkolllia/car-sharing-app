package org.project.carsharingapp.properties;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments")
public record PaymentProperties(
        BigDecimal fineMultiplier,
        Stripe stripe
) {
    public record Stripe(String secretKey) {}
}
