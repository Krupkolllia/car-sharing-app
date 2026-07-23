package org.project.carsharingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.project.carsharingapp.util.TestDataHelper.ADD_PAYMENT_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_ID;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_MAIL;
import static org.project.carsharingapp.util.TestDataHelper.EXPIRED_PAYMENT_ID;
import static org.project.carsharingapp.util.TestDataHelper.EXPIRED_PAYMENT_SESSION_ID;
import static org.project.carsharingapp.util.TestDataHelper.EXPIRED_PAYMENT_SESSION_URL;
import static org.project.carsharingapp.util.TestDataHelper.MANAGER_MAIL;
import static org.project.carsharingapp.util.TestDataHelper.PENDING_PAYMENT_ID;
import static org.project.carsharingapp.util.TestDataHelper.PENDING_PAYMENT_SESSION_ID;
import static org.project.carsharingapp.util.TestDataHelper.PENDING_PAYMENT_SESSION_URL;
import static org.project.carsharingapp.util.TestDataHelper.createAnotherUser;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.project.carsharingapp.dto.payment.PaymentCallbackResponseDto;
import org.project.carsharingapp.dto.payment.PaymentSession;
import org.project.carsharingapp.dto.payment.PaymentSessionRequest;
import org.project.carsharingapp.dto.payment.PaymentSessionStatus;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentRequestDto;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentResponseDto;
import org.project.carsharingapp.exception.PaymentGatewayException;
import org.project.carsharingapp.mapper.RentalPaymentMapper;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.User;
import org.project.carsharingapp.repository.PaymentRepository;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.repository.UserRepository;
import org.project.carsharingapp.service.payment.PaymentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class RentalPaymentControllerTest extends AbstractControllerTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalPaymentMapper rentalPaymentMapper;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        GET /payments with existing user id should
        return matching payments in Page
        """)
    void getAll_WithExistingUserId_ShouldReturnMatchingPaymentsInPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        List<RentalPaymentResponseDto> expected = paymentRepository
            .findAllFilteredByUserId(CUSTOMER_ID, pageable)
            .map(rentalPaymentMapper::toDto)
            .toList();

        // When
        MvcResult result = mockMvc.perform(
            get("/payments")
                .param("user_id", CUSTOMER_ID.toString())
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
        )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalPaymentResponseDto[] actual = getPaymentsContent(result);

        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        GET /payments without user id should
        return all payments in page
        """)
    void getAll_WithoutUserId_ShouldReturnAllPaymentsInPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        List<RentalPaymentResponseDto> expected = paymentRepository
            .findAllFilteredByUserId(null, pageable)
            .map(rentalPaymentMapper::toDto)
            .toList();

        // When
        MvcResult result = mockMvc.perform(
                get("/payments")
                    .param("page", String.valueOf(pageable.getPageNumber()))
                    .param("size", String.valueOf(pageable.getPageSize()))
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalPaymentResponseDto[] actual = getPaymentsContent(result);

        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);

    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, 1, -1, 4, 3939, -3883, Long.MAX_VALUE, Long.MIN_VALUE})
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        GET /payments for CUSTOMER with any provided user id should
        return customer's own payments in page
        """)
    void getAll_ForCustomerWithAnyUserId_ShouldReturnCustomersOwnPaymentsInPage(Long userId) throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        List<RentalPaymentResponseDto> expected = paymentRepository
            .findAllFilteredByUserId(CUSTOMER_ID, pageable)
            .map(rentalPaymentMapper::toDto)
            .toList();

        // When
        MockHttpServletRequestBuilder request = get("/payments")
            .param("page", String.valueOf(pageable.getPageNumber()))
            .param("size", String.valueOf(pageable.getPageSize()));

        if (userId != null) {
            request.param("user_id", userId.toString());
        }

        MvcResult result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalPaymentResponseDto[] actual = getPaymentsContent(result);

        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(MANAGER_MAIL)
    @DisplayName("""
        GET /payments with non existing user id should
        return empty Page
        """)
    void getAll_WithNonExistingUserId_ShouldReturnEmptyPage() throws Exception {
        // Given
        Long id = 404L;

        Pageable pageable = PageRequest.of(0, 10);

        // When
        MvcResult result = mockMvc.perform(
                get("/payments")
                    .param("user_id", id.toString())
                    .param("page", String.valueOf(pageable.getPageNumber()))
                    .param("size", String.valueOf(pageable.getPageSize()))

            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalPaymentResponseDto[] actual = getPaymentsContent(result);

        assertThat(actual).isEmpty();

    }

    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /payments in a valid case should
        return response dto with status code 201
        """)
    void createPaymentSession_ValidCase_ShouldReturnResponseDtoAndStatusCode201() throws Exception {
        // Given
        Long rentalId = 1L;

        RentalPaymentRequestDto requestDto = new RentalPaymentRequestDto(
            rentalId, PaymentType.PAYMENT
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        when(paymentGateway.createSession(any(PaymentSessionRequest.class)))
            .thenReturn(new PaymentSession(
                PENDING_PAYMENT_SESSION_ID, PENDING_PAYMENT_SESSION_URL
            ));

        // When
        MvcResult result = mockMvc.perform(
                post("/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isCreated())
            .andReturn();

        // Then
        RentalPaymentResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalPaymentResponseDto.class);

        assertThat(actual.rentalId()).isEqualTo(rentalId);

        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        assertThat(actual.userId()).isEqualTo(rental.getUser().getId());

        assertThat(actual.type()).isEqualTo(PaymentType.PAYMENT);

        assertThat(actual.status()).isEqualTo(PaymentStatus.PENDING);

        assertThat(actual.sessionUrl()).isEqualTo(PENDING_PAYMENT_SESSION_URL);

        Payment createdPayment = paymentRepository.findById(actual.id()).orElseThrow();

        assertThat(createdPayment.getRental().getId()).isEqualTo(rentalId);

        assertThat(createdPayment.getType()).isEqualTo(PaymentType.PAYMENT);

        assertThat(createdPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        assertThat(createdPayment.getSessionUrl()).isEqualTo(PENDING_PAYMENT_SESSION_URL);

        assertThat(createdPayment.getSessionId()).isEqualTo(PENDING_PAYMENT_SESSION_ID);

        assertThat(actual.total()).isEqualTo(createdPayment.getTotal());

        ArgumentCaptor<PaymentSessionRequest> captor =
            ArgumentCaptor.forClass(PaymentSessionRequest.class);

        verify(paymentGateway).createSession(captor.capture());

        PaymentSessionRequest stripeRequest = captor.getValue();

        assertThat(stripeRequest.amount()).isEqualByComparingTo("359.91");

        assertThat(stripeRequest.currency()).isEqualTo("usd");

        assertThat(stripeRequest.productName()).isEqualTo("Payment for BMW M5");

        assertThat(stripeRequest.quantity()).isEqualTo(1L);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /payments with id of non-existing rental should
        return status code 404
        """)
    void createPaymentSession_WithInvalidRentalId_ShouldReturnStatusCode404() throws Exception {
        // Given
        Long invalidRentalId = 404L;

        RentalPaymentRequestDto requestDto = new RentalPaymentRequestDto(
            invalidRentalId, PaymentType.FINE
        );

        long expectedDatabaseSize = paymentRepository.count();

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        mockMvc.perform(
                post("/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isNotFound());

        // Then
        long actualDatabaseSize = paymentRepository.count();
        assertThat(actualDatabaseSize).isEqualTo(expectedDatabaseSize);

        verifyNoInteractions(paymentGateway);
    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        POST /payments when authenticated user id
        is not equal rental user id should
        return status code 404
        """)
    void createPaymentSession_WithAnotherUserRental_ShouldReturnStatusCode404() throws Exception {
        // Given
        Long rentalId = 1L;

        Rental rental = rentalRepository.findById(rentalId).orElseThrow();

        User anotherUser = userRepository.saveAndFlush(createAnotherUser());

        rental.setUser(anotherUser);
        rentalRepository.saveAndFlush(rental);

        long expectedDatabaseSize = paymentRepository.count();

        RentalPaymentRequestDto requestDto = new RentalPaymentRequestDto(
            rentalId, PaymentType.PAYMENT
        );

        String jsonRequest = jsonMapper.writeValueAsString(requestDto);

        // When
        mockMvc.perform(
                post("/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest)
            )
            .andExpect(status().isNotFound());

        // Then
        long actualDatabaseSize = paymentRepository.count();
        assertThat(actualDatabaseSize).isEqualTo(expectedDatabaseSize);

        verifyNoInteractions(paymentGateway);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /payments/success in a valid case should
        mark Payment as paid and return response dto with status code 200
        """)
    void handleSuccessPayment_ValidCase_ShouldMarkPaymentAsPaidAndReturnResponseDto() throws Exception {
        // Given
        when(paymentGateway.getStatus(PENDING_PAYMENT_SESSION_ID))
            .thenReturn(PaymentSessionStatus.PAID);

        // When
        MvcResult result = mockMvc.perform(
                get("/payments/success")
                    .param("session_id", PENDING_PAYMENT_SESSION_ID)
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalPaymentResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalPaymentResponseDto.class);

        Payment actualPayment = paymentRepository.findById(actual.id()).orElseThrow();

        assertThat(actual.status()).isEqualTo(PaymentStatus.PAID);

        assertThat(actualPayment.getStatus()).isEqualTo(PaymentStatus.PAID);

        assertThat(actualPayment.getSessionId()).isEqualTo(PENDING_PAYMENT_SESSION_ID);

        assertThat(actual.sessionUrl()).isEqualTo(actualPayment.getSessionUrl());

        verify(paymentGateway).getStatus(PENDING_PAYMENT_SESSION_ID);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /payments/success when no payments by session id should
        return status code 404
        """)
    void handleSuccessPayment_WithInvalidSessionId_ShouldReturnStatusCode404() throws Exception {
        // Given
        String unknownSessionId = "unknown-session-id";

        // When
        mockMvc.perform(
                get("/payments/success")
                    .param("session_id", unknownSessionId)
            )
            .andExpect(status().isNotFound());

        // Then
        verifyNoInteractions(paymentGateway);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /payments/success when payment gateway throws exception should
        return status code 502
        """)
    void handleSuccessPayment_WhenPaymentGatewayThrowsException_ShouldReturnStatusCode502() throws Exception {
        // Given
        when(paymentGateway.getStatus(PENDING_PAYMENT_SESSION_ID))
            .thenThrow(new PaymentGatewayException("Payment gateway is unavailable"));

        // When
        mockMvc.perform(
                get("/payments/success")
                    .param("session_id", PENDING_PAYMENT_SESSION_ID)
            )
            .andExpect(status().isBadGateway());


        // Then
        verify(paymentGateway).getStatus(PENDING_PAYMENT_SESSION_ID);

    }



    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /payments/success when payment is already paid should
        return response dto and status code 200
        """)
    void handleSuccessPayment_WhenPaymentIsAlreadyPaid_ShouldReturnResponseDtoAndStatusCode200() throws Exception {
        // Given
        when(paymentGateway.getStatus(PENDING_PAYMENT_SESSION_ID))
            .thenReturn(PaymentSessionStatus.PAID);

        Payment payment = paymentRepository.findById(PENDING_PAYMENT_ID).orElseThrow();

        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.saveAndFlush(payment);

        // When
        MvcResult result = mockMvc.perform(
                get("/payments/success")
                    .param("session_id", PENDING_PAYMENT_SESSION_ID)
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalPaymentResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalPaymentResponseDto.class);

        assertThat(actual.id()).isEqualTo(PENDING_PAYMENT_ID);

        assertThat(actual.status()).isEqualTo(PaymentStatus.PAID);

        Payment actualPayment = paymentRepository.findById(actual.id()).orElseThrow();

        assertThat(actualPayment.getStatus()).isEqualTo(PaymentStatus.PAID);

        assertThat(actualPayment.getSessionId()).isEqualTo(PENDING_PAYMENT_SESSION_ID);

        assertThat(actual.sessionUrl()).isEqualTo(actualPayment.getSessionUrl());

        verify(paymentGateway).getStatus(PENDING_PAYMENT_SESSION_ID);

    }

    @ParameterizedTest
    @EnumSource(
        value = PaymentSessionStatus.class,
        names = "PAID",
        mode = Mode.EXCLUDE
    )
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        GET /payments/success when session status
        is not PAID should return status code 409
        """)
    void handleSuccessPayment_WhenSessionStatusIsNotPaid_ShouldReturnStatusCode409(
            PaymentSessionStatus paymentSessionStatus) throws Exception {
        // Given
        when(paymentGateway.getStatus(PENDING_PAYMENT_SESSION_ID))
            .thenReturn(paymentSessionStatus);

        // When
        mockMvc.perform(
                get("/payments/success")
                    .param("session_id", PENDING_PAYMENT_SESSION_ID)
            )
            .andExpect(status().isConflict());

        // Then
        Payment actualPayment = paymentRepository.findById(PENDING_PAYMENT_ID)
            .orElseThrow();

        assertThat(actualPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentGateway).getStatus(PENDING_PAYMENT_SESSION_ID);

    }

    @Test
    @DisplayName("""
        GET /payments/cancel method always should
        return response callback and status code 200
        """)
    void handleCancel_Always_ShouldReturnResponseCallbackAndStatusCode200() throws Exception {
        // Given
        var expected = new PaymentCallbackResponseDto(
            "Payment was not completed."
                + " You can retry using the existing payment link before it expires."
        );

        // When
        MvcResult result = mockMvc.perform(
                get("/payments/cancel")
            )
            .andExpect(status().isOk())
            .andReturn();


        // Then
        PaymentCallbackResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), PaymentCallbackResponseDto.class);

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        renewPaymentSession method in a valid case should
        return response dto and status code 200
        """)
    void renewPaymentSession_ValidCase_ShouldReturnResponseDtoAndStatusCode200() throws Exception {
        // Given
        when(paymentGateway.createSession(any(PaymentSessionRequest.class)))
            .thenReturn(new PaymentSession(
                "new-test-session-id", "new-test-session-url")
            );

        // When
        MvcResult result = mockMvc.perform(
                post("/payments/renew")
                    .param("payment_id", EXPIRED_PAYMENT_ID.toString())
            )
            .andExpect(status().isOk())
            .andReturn();

        // Then
        RentalPaymentResponseDto actual = jsonMapper.readValue(
            result.getResponse().getContentAsString(), RentalPaymentResponseDto.class);

        Payment actualPayment = paymentRepository.findById(actual.id()).orElseThrow();

        assertThat(actual.status()).isEqualTo(PaymentStatus.PENDING);

        assertThat(actual.sessionUrl()).isEqualTo("new-test-session-url");

        assertThat(actualPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        assertThat(actualPayment.getSessionId()).isEqualTo("new-test-session-id");

        assertThat(actualPayment.getSessionUrl()).isEqualTo("new-test-session-url");

        verify(paymentGateway).createSession(any(PaymentSessionRequest.class));

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        renewPaymentSession method with id of non-existing payment should
        return status code 404
        """)
    void renewPaymentSession_WithInvalidPaymentId_ShouldReturnStatusCode404() throws Exception {
        // Given
        Long invalidPaymentId = 404L;

        // When
        mockMvc.perform(
                post("/payments/renew")
                    .param("payment_id", invalidPaymentId.toString())
            )
            .andExpect(status().isNotFound());

        // Then
        verifyNoInteractions(paymentGateway);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        renewPaymentSession method with payment id of another user should
        return status code 404
        """)
    void renewPaymentSession_WithPaymentIdOfAnotherUser_ShouldReturnStatusCode404() throws Exception {
        // Given
        Payment payment = paymentRepository.findById(EXPIRED_PAYMENT_ID)
            .orElseThrow();

        Rental rental = rentalRepository.findById(payment.getRental().getId())
            .orElseThrow();

        User anotherUser = userRepository.saveAndFlush(createAnotherUser());

        rental.setUser(anotherUser);
        rentalRepository.saveAndFlush(rental);

        // When
        mockMvc.perform(
                post("/payments/renew")
                    .param("payment_id", EXPIRED_PAYMENT_ID.toString())
            )
            .andExpect(status().isNotFound());

        // Then
        Payment actualPayment = paymentRepository.findById(EXPIRED_PAYMENT_ID).orElseThrow();

        assertThat(actualPayment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);

        assertThat(actualPayment.getSessionId()).isEqualTo(EXPIRED_PAYMENT_SESSION_ID);

        assertThat(actualPayment.getSessionUrl()).isEqualTo(EXPIRED_PAYMENT_SESSION_URL);

        verifyNoInteractions(paymentGateway);

    }

    @ParameterizedTest
    @EnumSource(
        value = PaymentStatus.class,
        names = "EXPIRED",
        mode = Mode.EXCLUDE
    )
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails(CUSTOMER_MAIL)
    @DisplayName("""
        renewPaymentSession method when payment status is not EXPIRED should
        return status code 409
        """)
    void renewPaymentSession_WhenPaymentStatusIsNotExpired_ShouldReturnStatusCode409(
            PaymentStatus paymentStatus) throws Exception {
        // Given
        Payment payment = paymentRepository.findById(EXPIRED_PAYMENT_ID)
            .orElseThrow();

        payment.setStatus(paymentStatus);
        paymentRepository.saveAndFlush(payment);

        // When
        mockMvc.perform(
                post("/payments/renew")
                    .param("payment_id", EXPIRED_PAYMENT_ID.toString())
            )
            .andExpect(status().isConflict());

        // Then
        Payment actualPayment = paymentRepository.findById(EXPIRED_PAYMENT_ID).orElseThrow();

        assertThat(actualPayment.getStatus()).isEqualTo(paymentStatus);

        verifyNoInteractions(paymentGateway);

    }

    private RentalPaymentResponseDto[] getPaymentsContent(MvcResult result) throws Exception {
        String content = jsonMapper
            .readTree(result.getResponse().getContentAsString())
            .get("content")
            .toString();

        return jsonMapper.readValue(content, RentalPaymentResponseDto[].class);
    }

}
