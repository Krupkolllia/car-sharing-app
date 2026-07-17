package org.project.carsharingapp.service.payment;

import org.project.carsharingapp.dto.payment.PaymentRequestDto;
import org.project.carsharingapp.dto.payment.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService<S extends PaymentRequestDto, R extends PaymentResponseDto> {

    Page<R> findAll(Long userId, Pageable pageable);

    R createPaymentSession(S requestDto);

    R handleSuccessPayment(String sessionId);

    void markExpiredPayments();

}
