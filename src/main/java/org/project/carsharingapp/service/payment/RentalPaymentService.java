package org.project.carsharingapp.service.payment;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentCalculationSource;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentRequestDto;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentResponseDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.exception.PaymentProcessingException;
import org.project.carsharingapp.mapper.RentalPaymentMapper;
import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.Role;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.PaymentRepository;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.project.carsharingapp.service.NotificationService;
import org.project.carsharingapp.service.payment.calculator.RentalPaymentCalculatorResolver;
import org.project.carsharingapp.util.MessageBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalPaymentService
        implements PaymentService<RentalPaymentRequestDto, RentalPaymentResponseDto> {

    private static final String CURRENCY = "usd";

    private static final Long QUANTITY = 1L;

    private final PaymentRepository paymentRepository;

    private final RentalPaymentMapper paymentMapper;

    private final RentalPaymentCalculatorResolver calculatorResolver;

    private final PaymentGateway paymentGateway;

    private final RentalRepository rentalRepository;

    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<RentalPaymentResponseDto> findAll(Long userId, Pageable pageable) {
        User currentUser = SecurityUtil.getAuthenticatedUser();

        if (currentUser.getRole() == Role.CUSTOMER) {
            userId = currentUser.getId();
        }

        return paymentRepository.findAllFilteredByUserId(userId, pageable)
            .map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public RentalPaymentResponseDto createPaymentSession(RentalPaymentRequestDto requestDto) {
        Rental rental = rentalRepository.findByIdWithCar(requestDto.rentalId()).orElseThrow(
                () -> new EntityNotFoundException(
                    "Cannot find a rental with id: " + requestDto.rentalId())
        );

        User currentUser = SecurityUtil.getAuthenticatedUser();

        if (currentUser.getRole() == Role.CUSTOMER
                && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot create payment for this rental");
        }

        PaymentType paymentType = requestDto.paymentType();

        var calculationSource = new RentalPaymentCalculationSource(
                rental.getCar().getDailyFee(),
                rental.getRentalDate(),
                rental.getReturnDate(),
                rental.getActualReturnDate()
        );

        BigDecimal amount = calculatorResolver
                .resolve(paymentType)
                .calculate(calculationSource);

        PaymentSession paymentSession = paymentGateway.createSession(
            new PaymentSessionRequest(
                amount,
                CURRENCY,
                buildProductName(rental, paymentType),
                QUANTITY
            )
        );

        Payment payment = new Payment()
                .setRental(rental)
                .setType(paymentType)
                .setStatus(PaymentStatus.PENDING)
                .setTotal(amount)
                .setSessionId(paymentSession.id())
                .setSessionUrl(paymentSession.url());

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public RentalPaymentResponseDto handleSuccessPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException(
                    "Cannot find payment for Stripe session: " + sessionId)
        );

        if (payment.getStatus() == PaymentStatus.PAID) {
            return paymentMapper.toDto(payment);
        }

        PaymentSessionStatus sessionStatus = paymentGateway.getStatus(sessionId);
        if (paymentGateway.getStatus(sessionId) != PaymentSessionStatus.PAID) {
            throw new PaymentProcessingException(
                "Payment was not paid. Session status: " + sessionStatus);
        }

        payment.setStatus(PaymentStatus.PAID);

        notificationService.sendNotification(
            MessageBuilder.buildRentalPaymentCompletedMessage(
                paymentMapper.toMessageDto(payment))
        );

        return paymentMapper.toDto(payment);
    }

    private String buildProductName(Rental rental, PaymentType paymentType) {
        Car car = rental.getCar();

        return StringUtils.capitalize(paymentType.name().toLowerCase())
                + " for " + car.getBrand() + " " + car.getModel();
    }

}
