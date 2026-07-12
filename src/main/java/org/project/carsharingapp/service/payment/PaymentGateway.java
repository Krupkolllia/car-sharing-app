package org.project.carsharingapp.service.payment;

import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;

public interface PaymentGateway {

    PaymentSession createSession(PaymentSessionRequest request);

    PaymentSessionStatus getStatus(String sessionId);

}
