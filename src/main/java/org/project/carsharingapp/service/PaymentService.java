package org.project.carsharingapp.service;

import org.project.carsharingapp.dto.payment.PaymentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    Page<PaymentResponseDto> findAll(Long userId, Pageable pageable);

}
