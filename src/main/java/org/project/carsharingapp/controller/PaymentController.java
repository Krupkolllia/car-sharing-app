package org.project.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.payment.PaymentResponseDto;
import org.project.carsharingapp.security.annotation.ManagerOrCustomer;
import org.project.carsharingapp.service.payment.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get payments by user id")
    @ManagerOrCustomer
    @GetMapping
    public Page<PaymentResponseDto> getAll(
            @RequestParam(name = "user_id", required = false) Long userId,
            Pageable pageable
    ) {
        return paymentService.findAll(userId, pageable);
    }

}

