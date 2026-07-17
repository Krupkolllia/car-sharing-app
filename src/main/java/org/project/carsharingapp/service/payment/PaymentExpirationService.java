package org.project.carsharingapp.service.payment;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.repository.PaymentRepository;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentExpirationService {

    private final PaymentRepository paymentRepository;

    private final PaymentGateway paymentGateway;

    public void markExpiredPayments() {
        List<Payment> expiredPayments = new ArrayList<>();
        List<Payment> pendingPayments = paymentRepository
                .findAllByStatus(PaymentStatus.PENDING);

        for (Payment payment : pendingPayments) {
            try {
                PaymentSessionStatus paymentSessionStatus = paymentGateway
                        .getStatus(payment.getSessionId());

                if (paymentSessionStatus == PaymentSessionStatus.EXPIRED) {
                    payment.setStatus(PaymentStatus.EXPIRED);
                    expiredPayments.add(payment);
                }
            } catch (RuntimeException e) {
                log.warn(
                        "Failed to retrieve payment session. paymentId={}, sessionId={}",
                        payment.getId(),
                        payment.getSessionId(),
                        e
                );
            }

            if (!expiredPayments.isEmpty()) {
                paymentRepository.saveAll(expiredPayments);
            }

        }
    }

}
