package org.project.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentRequestDto;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentResponseDto;
import org.project.carsharingapp.security.annotation.ManagerOrCustomer;
import org.project.carsharingapp.service.payment.RentalPaymentService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class RentalPaymentController {

    private final RentalPaymentService rentalPaymentService;

    @Operation(summary = "Get payments by user id")
    @ManagerOrCustomer
    @GetMapping
    public Page<RentalPaymentResponseDto> getAll(
            @RequestParam(name = "user_id", required = false) Long userId,
            @ParameterObject Pageable pageable
    ) {
        return rentalPaymentService.findAll(userId, pageable);
    }

    @Operation(summary = "Create payment session")
    @ManagerOrCustomer
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RentalPaymentResponseDto createPaymentSession(
            @RequestBody @Valid RentalPaymentRequestDto requestDto) {
        return rentalPaymentService.createPaymentSession(requestDto);
    }

    @Operation(summary = "Redirect after successful payment")
    @GetMapping("/success")
    public RentalPaymentResponseDto handleSuccess(@RequestParam("session_id") String sessionId) {
        return rentalPaymentService.handleSuccessPayment(sessionId);
    }

}

