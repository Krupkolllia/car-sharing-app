package org.project.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.payment.PaymentResponseDto;
import org.project.carsharingapp.mapper.PaymentMapper;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.PaymentRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StripePaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<PaymentResponseDto> findAll(Long userId, Pageable pageable) {
        User currentUser = SecurityUtil.getAuthenticatedUser();

        if (currentUser.getRole() == Role.CUSTOMER) {
            userId = currentUser.getId();
        }

        return paymentRepository.findAllFilteredByUserId(userId, pageable)
            .map(paymentMapper::toDto);
    }
}
