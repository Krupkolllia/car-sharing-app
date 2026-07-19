package org.project.carsharingapp.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app",
        name = "scheduling-enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final PaymentExpirationService paymentExpirationService;

    @Scheduled(cron = "0 * * * * *")
    public void expirePaymentSessions() {
        paymentExpirationService.markExpiredPayments();
        log.debug("Expired payment sessions were processed");
    }

}
