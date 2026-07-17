package org.project.carsharingapp.service.payment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final List<PaymentService<?, ?>> paymentServices;

    @Scheduled(cron = "0 * * * * *")
    public void expirePaymentSessions() {
        paymentServices.forEach(PaymentService::markExpiredPayments);
        log.debug("Expired payment sessions were processed");
    }

}
