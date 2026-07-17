package org.project.carsharingapp.service.payment;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.project.carsharingapp.service.notifications.NotificationRequestedEvent;
import org.project.carsharingapp.service.payment.calculator.RentalPaymentCalculatorResolver;
import org.project.carsharingapp.util.MessageBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class RentalPaymentService
        implements PaymentService<RentalPaymentRequestDto, RentalPaymentResponseDto> {

    private static final Set<PaymentStatus> BLOCKING_STATUSES =
            EnumSet.of(
                PaymentStatus.PENDING
            );

    private static final String CURRENCY = "usd";

    private static final Long QUANTITY = 1L;

    private final ApplicationEventPublisher eventPublisher;

    private final TransactionTemplate transactionTemplate;

    private final PaymentRepository paymentRepository;

    private final RentalPaymentMapper paymentMapper;

    private final RentalPaymentCalculatorResolver calculatorResolver;

    private final PaymentGateway paymentGateway;

    private final RentalRepository rentalRepository;

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
    public RentalPaymentResponseDto createPaymentSession(RentalPaymentRequestDto requestDto) {
        User currentUser = SecurityUtil.getAuthenticatedUser();

        PreparedPayment preparedPayment = transactionTemplate.execute(status ->
                preparePayment(requestDto, currentUser)
        );

        PaymentSession paymentSession = paymentGateway.createSession(
            new PaymentSessionRequest(
                preparedPayment.amount(),
                CURRENCY,
                preparedPayment.productName(),
                QUANTITY
            )
        );

        RentalPaymentResponseDto responseDto;

        try {
            responseDto = transactionTemplate.execute(status -> {
                Rental rentalProxy = rentalRepository.getReferenceById(preparedPayment.rentalId());

                Payment payment = new Payment()
                        .setRental(rentalProxy)
                        .setType(preparedPayment.paymentType())
                        .setStatus(PaymentStatus.PENDING)
                        .setTotal(preparedPayment.amount())
                        .setSessionId(paymentSession.id())
                        .setSessionUrl(paymentSession.url());

                paymentRepository.save(payment);

                return paymentMapper.toDto(payment);
            });
        } catch (RuntimeException persistenceException) {
            log.error("Failed to persist payment. Stripe session was expired: sessionId={}",
                paymentSession.id(),
                persistenceException
            );

            expirePaymentSession(paymentSession.id(), persistenceException);

            throw persistenceException;
        }

        log.info("Payment created: sessionId={}, productName={},"
                + " userId={}, rentalId={}, type={}, total={}",
                paymentSession.id(),
                preparedPayment.productName(),
                currentUser.getId(),
                preparedPayment.rentalId(),
                preparedPayment.paymentType(),
                preparedPayment.amount()
        );

        return responseDto;

    }

    @Override
    public RentalPaymentResponseDto handleSuccessPayment(String sessionId) {
        PaymentSessionStatus sessionStatus = paymentGateway.getStatus(sessionId);

        RentalPaymentResponseDto responseDto = transactionTemplate.execute(status -> {
            Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                    () -> new EntityNotFoundException(
                        "Cannot find payment for Stripe session: " + sessionId)
            );

            if (payment.getStatus() == PaymentStatus.PAID) {
                return paymentMapper.toDto(payment);
            }

            if (sessionStatus != PaymentSessionStatus.PAID) {
                throw new PaymentProcessingException(
                    "Payment was not paid. Session status: " + sessionStatus);
            }

            payment.setStatus(PaymentStatus.PAID);

            publishNotificationRequestedEvent(
                    MessageBuilder.buildRentalPaymentCompletedMessage(
                        paymentMapper.toMessageDto(payment)
                    )
            );

            return paymentMapper.toDto(payment);
        });

        log.info(
                "Payment completed: paymentId={}, sessionId={}, paymentStatus={}",
                responseDto.id(),
                sessionId,
                responseDto.status()
        );

        return responseDto;

    }

    @Transactional(readOnly = true)
    public boolean hasUnpaidPayment(Long userId) {
        return paymentRepository
            .existsByRentalUserIdAndStatusNotIn(userId, BLOCKING_STATUSES);
    }

    private String buildProductName(Rental rental, PaymentType paymentType) {
        Car car = rental.getCar();

        return StringUtils.capitalize(paymentType.name().toLowerCase())
                + " for " + car.getBrand() + " " + car.getModel();
    }

    private PreparedPayment preparePayment(RentalPaymentRequestDto requestDto, User user) {
        Rental rental = rentalRepository.findByIdWithCar(requestDto.rentalId()).orElseThrow(
                () -> new EntityNotFoundException(
                    "Cannot find a rental with id: " + requestDto.rentalId())
        );

        if (user.getRole() == Role.CUSTOMER
                && !rental.getUser().getId().equals(user.getId())) {
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

        return new PreparedPayment(
            rental.getId(),
            paymentType,
            amount,
            buildProductName(rental, paymentType)
        );
    }

    private void expirePaymentSession(String sessionId, RuntimeException persistenceException) {
        try {
            paymentGateway.expireSession(sessionId);
        } catch (RuntimeException expirationException) {
            log.error(
                    "Failed to expire orphan Stripe session: sessionId={}. "
                        + "Original persistence error: {}",
                    sessionId,
                    persistenceException.getMessage(),
                    expirationException
            );
        }
    }

    private void publishNotificationRequestedEvent(String message) {
        eventPublisher.publishEvent(
            new NotificationRequestedEvent(message)
        );
    }

    private record PreparedPayment(
            Long rentalId,
            PaymentType paymentType,
            BigDecimal amount,
            String productName
    ) {}

}
