package org.project.carsharingapp.config;

import com.stripe.StripeClient;
import org.project.carsharingapp.properties.PaymentProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Bean
    public StripeClient stripeClient(PaymentProperties paymentProperties) {
        return new StripeClient(paymentProperties.stripe().secretKey());
    }

}
