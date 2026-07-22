package org.project.carsharingapp.service.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_ID;
import static org.project.carsharingapp.util.TestDataHelper.createAnotherUser;
import static org.project.carsharingapp.util.TestDataHelper.createPaymentSessionRequest;
import static org.project.carsharingapp.util.TestDataHelper.createPendingPayment;
import static org.project.carsharingapp.util.TestDataHelper.createPendingRentalPaymentResponseDto;
import static org.project.carsharingapp.util.TestDataHelper.createRental;
import static org.project.carsharingapp.util.TestDataHelper.createRentalPaymentCalculationSource;
import static org.project.carsharingapp.util.TestDataHelper.createRentalPaymentMessageDto;
import static org.project.carsharingapp.util.TestDataHelper.createTestCustomer;
import static org.project.carsharingapp.util.TestDataHelper.createTestManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentCalculationSource;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentMessageDto;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentRequestDto;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentResponseDto;
import org.project.carsharingapp.exception.EntityNotFoundException;
import org.project.carsharingapp.exception.PaymentGatewayException;
import org.project.carsharingapp.exception.PaymentNotExpiredException;
import org.project.carsharingapp.exception.PaymentProcessingException;
import org.project.carsharingapp.mapper.RentalPaymentMapper;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.repository.PaymentRepository;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.security.SecurityUtil;
import org.project.carsharingapp.service.notifications.NotificationRequestedEvent;
import org.project.carsharingapp.service.payment.calculator.RentalPaymentCalculatorResolver;
import org.project.carsharingapp.service.payment.calculator.RentalRegularPaymentCalculator;
import org.project.carsharingapp.util.MessageBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
public class RentalPaymentServiceTest {

    private static final String NEW_SESSION_ID = "new-session-id";

    private static final String NEW_SESSION_URL = "new-session-url";

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private TransactionStatus transactionStatus;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalPaymentMapper paymentMapper;

    @Mock
    private RentalPaymentCalculatorResolver calculatorResolver;

    @Mock
    private RentalRegularPaymentCalculator regularPaymentCalculator;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private RentalPaymentService rentalPaymentService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);

        securityUtilMock.when(SecurityUtil::getAuthenticatedUser)
            .thenReturn(createTestCustomer());

    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Nested
    class FindAllMethod {

        @Test
        @DisplayName("""
        findAll method for MANAGER when user id was provided should
        return page of matching payments
        """)
        void findAll_ForManagerWhenUserIdWasProvided_ShouldReturnPageOfMatchingPayments() {
            // Given
            authenticateAsManager();

            Payment payment = createPendingPayment();
            Long userId = payment.getRental().getUser().getId();

            RentalPaymentResponseDto expected = createPendingRentalPaymentResponseDto();

            Pageable pageable = PageRequest.of(0, 10);

            when(paymentRepository.findAllFilteredByUserId(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(payment)));

            when(paymentMapper.toDto(payment)).thenReturn(expected);

            // When
            Page<RentalPaymentResponseDto> actual = rentalPaymentService.findAll(userId, pageable);

            // Then
            assertThat(actual.getContent()).containsExactly(expected);

            verify(paymentRepository).findAllFilteredByUserId(userId, pageable);

            verify(paymentMapper).toDto(payment);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(paymentRepository, paymentMapper);

        }

        @Test
        @DisplayName("""
        findAll method for MANAGER when user id was not provided should
        return page of all payments
        """)
        void findAll_ForManagerWhenUserIdWasNotProvided_ShouldReturnPageOfAllPayments() {
            // Given
            authenticateAsManager();

            Payment payment = createPendingPayment();

            RentalPaymentResponseDto expected = createPendingRentalPaymentResponseDto();

            Pageable pageable = PageRequest.of(0, 10);

            when(paymentRepository.findAllFilteredByUserId(null, pageable))
                .thenReturn(new PageImpl<>(List.of(payment)));

            when(paymentMapper.toDto(payment)).thenReturn(expected);

            // When
            Page<RentalPaymentResponseDto> actual = rentalPaymentService.findAll(null, pageable);

            // Then
            assertThat(actual.getContent()).containsExactly(expected);

            verify(paymentRepository).findAllFilteredByUserId(null, pageable);

            verify(paymentMapper).toDto(payment);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(paymentRepository, paymentMapper);

        }

        @ParameterizedTest
        @NullSource
        @ValueSource(longs = {-10L, 0L, 1L, 404L, Long.MAX_VALUE, Long.MIN_VALUE})
        @DisplayName("""
        findAll method for CUSTOMER with any user id or null should
        return page of all customer's own payments
        """)
        void findAll_ForCustomerWithAnyUserIdOrNull_ShouldReturnPageOfAllCustomerOwnPayments(Long userId) {
            // Given
            Payment payment = createPendingPayment();

            RentalPaymentResponseDto expected = createPendingRentalPaymentResponseDto();

            Pageable pageable = PageRequest.of(0, 10);

            when(paymentRepository.findAllFilteredByUserId(CUSTOMER_ID, pageable))
                .thenReturn(new PageImpl<>(List.of(payment)));

            when(paymentMapper.toDto(payment)).thenReturn(expected);

            // When
            Page<RentalPaymentResponseDto> actual = rentalPaymentService.findAll(userId, pageable);

            // Then
            assertThat(actual.getContent()).containsExactly(expected);

            verify(paymentRepository).findAllFilteredByUserId(CUSTOMER_ID, pageable);

            verify(paymentMapper).toDto(payment);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(paymentRepository, paymentMapper);

        }
    }

    @Nested
    class CreatePaymentSessionMethod {

        @Test
        @DisplayName("""
        createPaymentSession method in a valid case should
        return created payment
        """)
        void createPaymentSession_ValidCase_ShouldReturnCreatedPayment() {
            // Given
            stubTransactionTemplate();

            RentalPaymentResponseDto expected = createPendingRentalPaymentResponseDto();

            Rental rental = createRental();

            Rental proxyRental = new Rental().setId(rental.getId());

            Payment payment = createPendingPayment().setRental(proxyRental);

            PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
                .withAmount(payment.getTotal())
                .withProductName("Payment for BMW M5");

            PaymentSession paymentSession = new PaymentSession(
                payment.getSessionId(), payment.getSessionUrl()
            );

            RentalPaymentCalculationSource calculationSource = createRentalPaymentCalculationSource(rental);

            RentalPaymentRequestDto rentalPaymentRequestDto = new RentalPaymentRequestDto(
                rental.getId(), PaymentType.PAYMENT
            );

            when(rentalRepository.findByIdWithCar(rental.getId()))
                .thenReturn(Optional.of(rental));

            when(calculatorResolver.resolve(PaymentType.PAYMENT))
                .thenReturn(regularPaymentCalculator);

            when(regularPaymentCalculator.calculate(calculationSource)).thenReturn(expected.total());

            when(paymentGateway.createSession(paymentSessionRequest))
                .thenReturn(paymentSession);

            when(rentalRepository.getReferenceById(rental.getId()))
                .thenReturn(proxyRental);

            when(paymentMapper.toDto(any(Payment.class))).thenReturn(expected);

            // When
            RentalPaymentResponseDto actual = rentalPaymentService
                .createPaymentSession(rentalPaymentRequestDto);

            // Then
            assertThat(actual).isEqualTo(expected);

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment savedPayment = paymentCaptor.getValue();
            assertThat(savedPayment)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(payment);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(rentalRepository).findByIdWithCar(rental.getId());

            verify(calculatorResolver).resolve(PaymentType.PAYMENT);

            verify(regularPaymentCalculator).calculate(calculationSource);

            verify(paymentGateway).createSession(paymentSessionRequest);

            verify(rentalRepository).getReferenceById(rental.getId());

            verify(paymentMapper).toDto(savedPayment);

            verify(transactionTemplate, times(2)).execute(any());

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(
                transactionTemplate,
                rentalRepository,
                calculatorResolver,
                regularPaymentCalculator,
                paymentGateway,
                paymentRepository,
                paymentMapper
            );

        }

        @Test
        @DisplayName("""
        createPaymentSession method with id of non-existing rental should
        throw EntityNotFoundException
        """)
        void createPaymentSession_WithInvalidRentalId_ShouldThrowEntityNotFoundException() {
            // Given
            Long invalidRentalId = 404L;

            stubTransactionTemplate();

            RentalPaymentRequestDto rentalPaymentRequestDto = new RentalPaymentRequestDto(
                invalidRentalId, PaymentType.PAYMENT
            );

            when(rentalRepository.findByIdWithCar(invalidRentalId))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.createPaymentSession(rentalPaymentRequestDto))
                .isExactlyInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cannot find a rental with id: " + invalidRentalId);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(transactionTemplate).execute(any());

            verify(rentalRepository).findByIdWithCar(invalidRentalId);

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(rentalRepository);

            verifyNoInteractions(
                calculatorResolver,
                regularPaymentCalculator,
                paymentRepository,
                paymentGateway,
                paymentMapper
            );

        }

        @Test
        @DisplayName("""
        createPaymentSession method with another user's rental should
        hide rental existence and throw EntityNotFoundException
        """)
        void createPaymentSession_WithAnotherUsersRental_ShouldThrowEntityNotFoundException() {
            // Given
            stubTransactionTemplate();

            Rental rental = createRental();
            rental.setUser(createAnotherUser().setId(999L));

            RentalPaymentRequestDto rentalPaymentRequestDto = new RentalPaymentRequestDto(
                rental.getId(), PaymentType.PAYMENT
            );

            when(rentalRepository.findByIdWithCar(rental.getId()))
                .thenReturn(Optional.of(rental));

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.createPaymentSession(rentalPaymentRequestDto))
                .isExactlyInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cannot find a rental with id: " + rental.getId());

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(transactionTemplate).execute(any());

            verify(rentalRepository).findByIdWithCar(rental.getId());

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(rentalRepository);

            verifyNoInteractions(
                calculatorResolver,
                regularPaymentCalculator,
                paymentRepository,
                paymentGateway,
                paymentMapper
            );

        }

        @Test
        @DisplayName("""
        createPaymentSession method when payment persistence fails should
        expire session and rethrow exception
        """)
        void createPaymentSession_WhenPaymentPersistenceFails_ShouldExpireSessionAndRethrowException() {
            // Given
            stubTransactionTemplate();

            Rental rental = createRental();

            Rental proxyRental = new Rental().setId(rental.getId());

            Payment payment = createPendingPayment().setRental(proxyRental);

            PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
                .withAmount(new BigDecimal("359.91"))
                .withProductName("Payment for BMW M5");

            PaymentSession paymentSession = new PaymentSession(
                payment.getSessionId(), payment.getSessionUrl()
            );

            RentalPaymentCalculationSource calculationSource = createRentalPaymentCalculationSource(rental);

            RentalPaymentRequestDto rentalPaymentRequestDto = new RentalPaymentRequestDto(
                rental.getId(), PaymentType.PAYMENT
            );

            RuntimeException persistenceException = new RuntimeException("Database error");

            when(rentalRepository.findByIdWithCar(rental.getId()))
                .thenReturn(Optional.of(rental));

            when(calculatorResolver.resolve(PaymentType.PAYMENT))
                .thenReturn(regularPaymentCalculator);

            when(regularPaymentCalculator.calculate(calculationSource)).thenReturn(payment.getTotal());

            when(paymentGateway.createSession(paymentSessionRequest))
                .thenReturn(paymentSession);

            when(rentalRepository.getReferenceById(rental.getId()))
                .thenReturn(proxyRental);

            when(paymentRepository.save(any(Payment.class))).thenThrow(persistenceException);

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.createPaymentSession(rentalPaymentRequestDto))
                .isSameAs(persistenceException);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(rentalRepository).findByIdWithCar(rental.getId());

            verify(calculatorResolver).resolve(PaymentType.PAYMENT);

            verify(regularPaymentCalculator).calculate(calculationSource);

            verify(paymentGateway).createSession(paymentSessionRequest);

            verify(rentalRepository).getReferenceById(rental.getId());

            verify(paymentRepository).save(any(Payment.class));

            verify(paymentGateway).expireSession(paymentSession.id());

            verify(transactionTemplate, times(2)).execute(any());

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(
                transactionTemplate,
                rentalRepository,
                calculatorResolver,
                regularPaymentCalculator,
                paymentGateway,
                paymentRepository
            );

            verifyNoInteractions(paymentMapper);

        }

        @Test
        @DisplayName("""
        createPaymentSession method when persistence and session expiration fail should
        rethrow original persistence exception
        """)
        void createPaymentSession_WhenPersistenceAndExpirationFail_ShouldRethrowOriginalException() {
            // Given
            stubTransactionTemplate();

            Rental rental = createRental();

            Rental proxyRental = new Rental().setId(rental.getId());

            Payment payment = createPendingPayment().setRental(proxyRental);

            PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
                .withAmount(new BigDecimal("359.91"))
                .withProductName("Payment for BMW M5");

            PaymentSession paymentSession = new PaymentSession(
                payment.getSessionId(), payment.getSessionUrl()
            );

            RentalPaymentCalculationSource calculationSource = createRentalPaymentCalculationSource(rental);

            RentalPaymentRequestDto rentalPaymentRequestDto = new RentalPaymentRequestDto(
                rental.getId(), PaymentType.PAYMENT
            );

            RuntimeException persistenceException = new RuntimeException("Database error");
            RuntimeException expirationException = new RuntimeException("Gateway error");

            when(rentalRepository.findByIdWithCar(rental.getId()))
                .thenReturn(Optional.of(rental));

            when(calculatorResolver.resolve(PaymentType.PAYMENT))
                .thenReturn(regularPaymentCalculator);

            when(regularPaymentCalculator.calculate(calculationSource)).thenReturn(payment.getTotal());

            when(paymentGateway.createSession(paymentSessionRequest))
                .thenReturn(paymentSession);

            when(rentalRepository.getReferenceById(rental.getId()))
                .thenReturn(proxyRental);

            when(paymentRepository.save(any(Payment.class))).thenThrow(persistenceException);
            doThrow(expirationException).when(paymentGateway).expireSession(paymentSession.id());

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.createPaymentSession(rentalPaymentRequestDto))
                .isSameAs(persistenceException);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(rentalRepository).findByIdWithCar(rental.getId());

            verify(calculatorResolver).resolve(PaymentType.PAYMENT);

            verify(regularPaymentCalculator).calculate(calculationSource);

            verify(paymentGateway).createSession(paymentSessionRequest);

            verify(rentalRepository).getReferenceById(rental.getId());

            verify(paymentRepository).save(any(Payment.class));

            verify(paymentGateway).expireSession(paymentSession.id());

            verify(transactionTemplate, times(2)).execute(any());

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(
                transactionTemplate,
                rentalRepository,
                calculatorResolver,
                regularPaymentCalculator,
                paymentGateway,
                paymentRepository
            );

            verifyNoInteractions(paymentMapper);

        }
    }

    @Nested
    class HandleSuccessPaymentMethod {

        @Test
        @DisplayName("""
        handleSuccessPayment method in a valid case should
        mark payment paid and return dto
        """)
        void handleSuccessPayment_ValidCase_ShouldMarkPaymentPaidAndReturnDto() {
            // Given
            stubTransactionTemplate();

            Payment payment = createPendingPayment();
            String sessionId = payment.getSessionId();

            RentalPaymentMessageDto messageDto = createRentalPaymentMessageDto()
                .withPaymentStatus(PaymentStatus.PAID);
            String message = MessageBuilder.buildRentalPaymentCompletedMessage(messageDto);

            RentalPaymentResponseDto expected = createPendingRentalPaymentResponseDto()
                .withStatus(PaymentStatus.PAID);

            when(paymentRepository.existsBySessionId(sessionId))
                .thenReturn(true);

            when(paymentGateway.getStatus(sessionId))
                .thenReturn(PaymentSessionStatus.PAID);

            when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.of(payment));

            when(paymentMapper.toMessageDto(payment))
                .thenReturn(messageDto);

            when(paymentMapper.toDto(payment))
                .thenReturn(expected);

            // When
            RentalPaymentResponseDto actual = rentalPaymentService.handleSuccessPayment(sessionId);

            // Then
            assertThat(actual).isEqualTo(expected);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

            verify(paymentRepository).existsBySessionId(sessionId);

            verify(paymentGateway).getStatus(sessionId);

            verify(paymentRepository).findBySessionId(sessionId);

            verify(paymentMapper).toMessageDto(payment);

            verify(eventPublisher).publishEvent(new NotificationRequestedEvent(message));

            verify(transactionTemplate).execute(any());

            verify(paymentMapper).toDto(payment);

            verifyNoMoreInteractions(
                paymentRepository,
                paymentGateway,
                paymentMapper,
                transactionTemplate,
                eventPublisher
            );

        }

        @Test
        @DisplayName("""
        handleSuccessPayment method when no payment exists by session id should
        throw EntityNotFoundException
        """)
        void handleSuccessPayment_WhenNoPaymentExistBySessionId_ShouldThrowEntityNotFoundException() {
            // Given
            String nonExistingSessionId = "no-existing-session-id";

            when(paymentRepository.existsBySessionId(nonExistingSessionId))
                .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.handleSuccessPayment(nonExistingSessionId))
                .isExactlyInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cannot find payment for provided Stripe session");

            verify(paymentRepository).existsBySessionId(nonExistingSessionId);

            verifyNoMoreInteractions(paymentRepository);

            verifyNoInteractions(
                paymentGateway,
                transactionTemplate,
                paymentMapper,
                eventPublisher
            );

        }

        @Test
        @DisplayName("""
        handleSuccessPayment method when payment disappears after existence check should
        throw EntityNotFoundException
        """)
        void handleSuccessPayment_WhenPaymentDisappears_ShouldThrowEntityNotFoundException() {
            // Given
            stubTransactionTemplate();

            String sessionId = "test-session-id";

            when(paymentRepository.existsBySessionId(sessionId))
                .thenReturn(true);

            when(paymentGateway.getStatus(sessionId)).thenReturn(PaymentSessionStatus.PAID);

            when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.handleSuccessPayment(sessionId))
                .isExactlyInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cannot find payment for provided Stripe session");

            verify(paymentRepository).existsBySessionId(sessionId);

            verify(paymentGateway).getStatus(sessionId);

            verify(transactionTemplate).execute(any());

            verify(paymentRepository).findBySessionId(sessionId);

            verifyNoMoreInteractions(paymentRepository);

            verifyNoInteractions(paymentMapper, eventPublisher);

        }

        @Test
        @DisplayName("""
        handleSuccessPayment method when payment is already paid should
        return existing payment without notification
        """)
        void handleSuccessPayment_WhenPaymentIsAlreadyPaid_ShouldReturnExistingPaymentWithoutNotification() {
            // Given
            stubTransactionTemplate();

            Payment payment = createPendingPayment().setStatus(PaymentStatus.PAID);
            String sessionId = payment.getSessionId();

            RentalPaymentResponseDto expected = createPendingRentalPaymentResponseDto()
                .withStatus(PaymentStatus.PAID);

            when(paymentRepository.existsBySessionId(sessionId))
                .thenReturn(true);

            when(paymentGateway.getStatus(sessionId))
                .thenReturn(PaymentSessionStatus.UNPAID);

            when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.of(payment));

            when(paymentMapper.toDto(payment))
                .thenReturn(expected);

            // When
            RentalPaymentResponseDto actual = rentalPaymentService.handleSuccessPayment(sessionId);

            // Then
            assertThat(actual).isEqualTo(expected);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

            verify(paymentRepository).existsBySessionId(sessionId);

            verify(paymentGateway).getStatus(sessionId);

            verify(transactionTemplate).execute(any());

            verify(paymentRepository).findBySessionId(sessionId);

            verify(paymentMapper).toDto(payment);

            verify(paymentMapper, never()).toMessageDto(any(Payment.class));

            verifyNoMoreInteractions(
                paymentRepository,
                paymentGateway,
                transactionTemplate,
                paymentMapper
            );

            verifyNoInteractions(eventPublisher);

        }

        @ParameterizedTest
        @EnumSource(
            value = PaymentSessionStatus.class,
            names = "PAID",
            mode = Mode.EXCLUDE
        )
        @DisplayName("""
        handleSuccessPayment method for non-paid gateway session should
        throw PaymentProcessingException
        """)
        void handleSuccessPayment_ForNonPaidGatewaySession_ShouldThrowPaymentProcessingException(
            PaymentSessionStatus sessionStatus
        ) {
            // Given
            stubTransactionTemplate();

            Payment payment = createPendingPayment();
            String sessionId = payment.getSessionId();

            when(paymentRepository.existsBySessionId(sessionId))
                .thenReturn(true);

            when(paymentGateway.getStatus(sessionId))
                .thenReturn(sessionStatus);

            when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.handleSuccessPayment(sessionId))
                .isExactlyInstanceOf(PaymentProcessingException.class)
                .hasMessage("Payment was not paid. Session status: " + sessionStatus);


            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            verify(paymentRepository).existsBySessionId(sessionId);

            verify(paymentGateway).getStatus(sessionId);

            verify(transactionTemplate).execute(any());

            verify(paymentRepository).findBySessionId(sessionId);

            verifyNoMoreInteractions(paymentRepository, paymentGateway, transactionTemplate);

            verifyNoInteractions(paymentMapper, eventPublisher);

        }
    }

    @Nested
    class RenewSessionMethod {

        @Test
        @DisplayName("""
        renewSession method in a valid case should
        replace payment session and mark payment pending
        """)
        void renewSession_ValidCase_ShouldReplacePaymentSessionAndMarkPaymentPending() {
            // Given
            stubTransactionTemplate();

            Payment payment = createPendingPayment().setStatus(PaymentStatus.EXPIRED);
            Long userId = payment.getRental().getUser().getId();

            PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
                .withAmount(payment.getTotal())
                .withProductName("Payment for BMW M5");

            PaymentSession newSession = new PaymentSession(
                NEW_SESSION_ID, NEW_SESSION_URL
            );

            RentalPaymentResponseDto expected = createPendingRentalPaymentResponseDto()
                .withSessionUrl(NEW_SESSION_URL);

            when(paymentRepository.findByIdAndRentalUserId(payment.getId(), userId))
                .thenReturn(Optional.of(payment));

            when(paymentGateway.createSession(paymentSessionRequest))
                .thenReturn(newSession);

            when(paymentMapper.toDto(payment))
                .thenReturn(expected);

            // When
            RentalPaymentResponseDto actual = rentalPaymentService.renewSession(payment.getId());

            // Then
            assertThat(actual).isEqualTo(expected);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getSessionId()).isEqualTo(NEW_SESSION_ID);
            assertThat(payment.getSessionUrl()).isEqualTo(NEW_SESSION_URL);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(paymentRepository).findByIdAndRentalUserId(payment.getId(), userId);

            verify(paymentGateway).createSession(paymentSessionRequest);

            verify(paymentRepository).save(payment);

            verify(paymentMapper).toDto(payment);

            verify(transactionTemplate).execute(any());

            securityUtilMock.verifyNoMoreInteractions();

            verifyNoMoreInteractions(
                paymentRepository,
                paymentGateway,
                paymentMapper,
                transactionTemplate
            );

        }

        @Test
        @DisplayName("""
        renewSession method with id of non-existing payment should
        throw EntityNotFoundException
        """)
        void renewSession_WithInvalidPaymentId_ShouldThrowEntityNotFoundException() {
            // Given
            Long invalidPaymentId = 404L;

            when(paymentRepository.findByIdAndRentalUserId(invalidPaymentId, CUSTOMER_ID))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.renewSession(invalidPaymentId))
                .isExactlyInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cannot find payment with id: " + invalidPaymentId);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(paymentRepository).findByIdAndRentalUserId(invalidPaymentId, CUSTOMER_ID);

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(paymentRepository);

            verifyNoInteractions(
                paymentGateway,
                paymentMapper,
                transactionTemplate
            );

        }

        @ParameterizedTest
        @EnumSource(
            value = PaymentStatus.class,
            names = "EXPIRED",
            mode = Mode.EXCLUDE
        )
        @DisplayName("""
        renewSession method when payment status is not EXPIRED should
        throw PaymentNotExpiredException
        """)
        void renewSession_WhenPaymentStatusIsNotExpired_ShouldThrowPaymentNotExpiredException(
            PaymentStatus paymentStatus
        ) {
            // Given
            Payment payment = createPendingPayment().setStatus(paymentStatus);
            Long userId = payment.getRental().getUser().getId();

            when(paymentRepository.findByIdAndRentalUserId(payment.getId(), userId))
                .thenReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.renewSession(payment.getId()))
                .isExactlyInstanceOf(PaymentNotExpiredException.class)
                .hasMessage("Only expired payment session can be renewed. Current status: "
                    + payment.getStatus());

            assertThat(payment.getStatus()).isEqualTo(paymentStatus);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(paymentRepository).findByIdAndRentalUserId(payment.getId(), userId);

            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(paymentRepository);

            verifyNoInteractions(paymentGateway, transactionTemplate, paymentMapper);

        }

        @Test
        @DisplayName("""
        renewSession method when payment gateway throws exception should
        leave expired payment unchanged
        """)
        void renewSession_WhenPaymentGatewayThrowsException_ShouldLeaveExpiredPaymentUnchanged() {
            // Given
            Payment payment = createPendingPayment().setStatus(PaymentStatus.EXPIRED);
            Long userId = payment.getRental().getUser().getId();

            String expectedSessionId = payment.getSessionId();
            String expectedSessionUrl = payment.getSessionUrl();

            PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
                .withAmount(new BigDecimal("359.91"))
                .withProductName("Payment for BMW M5");

            PaymentGatewayException paymentGatewayException =
                new PaymentGatewayException("Payment gateway error");

            when(paymentRepository.findByIdAndRentalUserId(payment.getId(), userId))
                .thenReturn(Optional.of(payment));

            when(paymentGateway.createSession(paymentSessionRequest))
                .thenThrow(paymentGatewayException);

            // When & Then
            assertThatThrownBy(() -> rentalPaymentService.renewSession(payment.getId()))
                .isSameAs(paymentGatewayException);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
            assertThat(payment.getSessionId()).isEqualTo(expectedSessionId);
            assertThat(payment.getSessionUrl()).isEqualTo(expectedSessionUrl);

            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(paymentRepository).findByIdAndRentalUserId(payment.getId(), userId);

            verify(paymentGateway).createSession(paymentSessionRequest);


            securityUtilMock.verifyNoMoreInteractions();
            verifyNoMoreInteractions(paymentRepository, paymentGateway);

            verifyNoInteractions(transactionTemplate, paymentMapper);

        }

        @Test
        @DisplayName("""
        renewSession method when payment persistence fails should
        expire new session and rethrow exception
        """)
        void renewSession_WhenPaymentPersistenceFails_ShouldExpireNewSessionAndRethrowException() {
            // Given
            stubTransactionTemplate();

            Payment payment = createPendingPayment().setStatus(PaymentStatus.EXPIRED);
            Long userId = payment.getRental().getUser().getId();

            PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
                .withAmount(payment.getTotal())
                .withProductName("Payment for BMW M5");

            PaymentSession newSession = new PaymentSession(
                NEW_SESSION_ID, NEW_SESSION_URL
            );

            RuntimeException persistenceException = new RuntimeException("Database error");

            when(paymentRepository.findByIdAndRentalUserId(payment.getId(), userId))
                .thenReturn(Optional.of(payment));

            when(paymentGateway.createSession(paymentSessionRequest))
                .thenReturn(newSession);

            when(paymentRepository.save(payment))
                .thenThrow(persistenceException);

            // When
            assertThatThrownBy(() -> rentalPaymentService.renewSession(payment.getId()))
                .isSameAs(persistenceException);

            // Then
            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(paymentRepository).findByIdAndRentalUserId(payment.getId(), userId);

            verify(paymentGateway).createSession(paymentSessionRequest);

            verify(paymentRepository).save(payment);

            verify(paymentGateway).expireSession(NEW_SESSION_ID);

            verify(transactionTemplate).execute(any());

            securityUtilMock.verifyNoMoreInteractions();

            verifyNoMoreInteractions(
                paymentRepository,
                paymentGateway,
                transactionTemplate
            );

            verifyNoInteractions(paymentMapper);

        }

        @Test
        @DisplayName("""
        renewSession method when payment persistence and session expiration fail
        should rethrow original persistence exception
        """)
        void renewSession_WhenPersistenceAndExpirationFail_ShouldRethrowOriginalException() {
            // Given
            stubTransactionTemplate();

            Payment payment = createPendingPayment().setStatus(PaymentStatus.EXPIRED);
            Long userId = payment.getRental().getUser().getId();

            PaymentSessionRequest paymentSessionRequest = createPaymentSessionRequest()
                .withAmount(payment.getTotal())
                .withProductName("Payment for BMW M5");

            PaymentSession newSession = new PaymentSession(
                NEW_SESSION_ID, NEW_SESSION_URL
            );

            RuntimeException persistenceException = new RuntimeException("Database error");
            RuntimeException expirationException = new RuntimeException("Payment gateway error");

            when(paymentRepository.findByIdAndRentalUserId(payment.getId(), userId))
                .thenReturn(Optional.of(payment));

            when(paymentGateway.createSession(paymentSessionRequest))
                .thenReturn(newSession);

            when(paymentRepository.save(payment))
                .thenThrow(persistenceException);

            doThrow(expirationException).when(paymentGateway).expireSession(NEW_SESSION_ID);

            // When
            assertThatThrownBy(() -> rentalPaymentService.renewSession(payment.getId()))
                .isSameAs(persistenceException);

            // Then
            securityUtilMock.verify(SecurityUtil::getAuthenticatedUser);

            verify(paymentRepository).findByIdAndRentalUserId(payment.getId(), userId);

            verify(paymentGateway).createSession(paymentSessionRequest);

            verify(paymentRepository).save(payment);

            verify(paymentGateway).expireSession(NEW_SESSION_ID);

            verify(transactionTemplate).execute(any());

            securityUtilMock.verifyNoMoreInteractions();

            verifyNoMoreInteractions(
                paymentRepository,
                paymentGateway,
                transactionTemplate
            );

            verifyNoInteractions(paymentMapper);

        }
    }

    @Nested
    class HasUnpaidPaymentMethod {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("""
        hasUnpaidPayment method should
        return repository result for blocking statuses
        """)
        void hasUnpaidPayment_ShouldReturnRepositoryResult(boolean expected) {
            // Given
            Set<PaymentStatus> blockingStatuses = Set.of(
                PaymentStatus.PENDING,
                PaymentStatus.EXPIRED
            );

            when(paymentRepository.existsByRentalUserIdAndStatusIn(CUSTOMER_ID, blockingStatuses))
                    .thenReturn(expected);

            // When
            boolean actual = rentalPaymentService.hasUnpaidPayment(CUSTOMER_ID);

            // Then
            assertThat(actual).isEqualTo(expected);

            verify(paymentRepository).existsByRentalUserIdAndStatusIn(CUSTOMER_ID, blockingStatuses);

            verifyNoMoreInteractions(paymentRepository);
        }
    }

    private void authenticateAsManager() {
        securityUtilMock.when(SecurityUtil::getAuthenticatedUser)
            .thenReturn(createTestManager());
    }

    private void stubTransactionTemplate() {
        doAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);

            return callback.doInTransaction(transactionStatus);
        }).when(transactionTemplate).execute(any());
    }

}
