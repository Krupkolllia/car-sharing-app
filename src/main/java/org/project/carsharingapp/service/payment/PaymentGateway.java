package org.project.carsharingapp.service.payment;

import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;

public interface PaymentGateway {

    PaymentSession createSession(PaymentSessionRequest request);

    void expireSession(String sessionId);

    PaymentSessionStatus getStatus(String sessionId);

}
