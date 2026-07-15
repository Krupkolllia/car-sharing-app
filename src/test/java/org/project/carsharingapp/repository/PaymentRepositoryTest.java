package org.project.carsharingapp.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.project.carsharingapp.util.TestDataHelper.ADD_PAYMENT_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.ADD_SCRIPT_PATH;
import static org.project.carsharingapp.util.TestDataHelper.CUSTOMER_ID;
import static org.project.carsharingapp.util.TestDataHelper.createPendingPayment;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllFilteredByUserId method with userId null should
        return all payments
        """)
    void findAllFilteredByUserId_WithNullUserId_ShouldReturnAllPayments() {
        // Given
        List<Payment> expected = paymentRepository.findAll();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Payment> actual = paymentRepository.findAllFilteredByUserId(null, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        findAllFilteredByUserId method with id of user with existing payments should
        return all user's payments
        """)
    void findAllFilteredByUserId_WithIdOfUserWithPayments_ShouldReturnAllUserPayments() {
        // Given
        List<Payment> expected = paymentRepository.findAll().stream()
            .filter(payment -> Objects.equals(payment.getRental().getUser().getId(), CUSTOMER_ID))
            .toList();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Payment> actual = paymentRepository.findAllFilteredByUserId(CUSTOMER_ID, pageable);

        // Then
        assertThat(actual.getContent())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)   
    @DisplayName("""
        findAllFilteredByUserId method with id of non-existing user should
        return empty Page
        """)
    void findAllFilteredByUserId_WithInvalidUserId_ShouldReturnEmptyPage() {
        // Given
        Long invalidId = 404L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Payment> actual = paymentRepository.findAllFilteredByUserId(invalidId, pageable);

        // Then
        assertThat(actual.getContent()).isEmpty();
    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)    
    @DisplayName("""
        findBySessionId method with existing session id should
        return found payment
        """)
    void findBySessionId_WithExistingSessionId_ShouldReturnFoundPayment() {
        // Given
        String sessionId = "testid1";
        Payment expected = paymentRepository.findAll().stream()
            .filter(payment -> payment.getSessionId().equals(sessionId))
            .findFirst()
            .orElseThrow();

        // When
        Optional<Payment> actual = paymentRepository.findBySessionId(sessionId);

        // Then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);

    }

    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)    
    @DisplayName("""
        findBySessionId method with non-existing session id should
        return empty Optional
        """)
    void findBySessionId_WithInvalidId_ShouldReturnEmptyOptional() {
        // Given
        String invalidSessionId = "invalid-session-id";

        // When
        Optional<Payment> actual = paymentRepository.findBySessionId(invalidSessionId);

        // Then
        assertThat(actual).isEmpty();

    }
    
    @Test
    @Sql(scripts = {
        ADD_SCRIPT_PATH, ADD_PAYMENT_SCRIPT_PATH
    }, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        existsByRentalUserIdAndStatusIn method when
        wanted payment exists should return boolean true
        """)
    void existsByRentalUserIdAndStatusIn_WhenPaymentExists_ShouldReturnThisPayment() {
        Long userId = createPendingPayment().getRental().getUser().getId();
        Collection<PaymentStatus> statuses = EnumSet.of(PaymentStatus.PENDING);
        
        // When
        boolean actual = paymentRepository.existsByRentalUserIdAndStatusIn(userId, statuses);
        
        // Then
        assertThat(actual).isTrue();
        
    }
    
    @Test
    @Sql(scripts = ADD_SCRIPT_PATH, executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("""
        existsByRentalUserIdAndStatusIn method when wanted payment does not exists should
        return boolean false
        """)
    void existsByRentalUserIdAndStatusIn_With_ShouldReturnBooleanFalse() {
        // Given
        Long userId = createPendingPayment().getRental().getUser().getId();
        Collection<PaymentStatus> statuses = EnumSet.of(
                PaymentStatus.PENDING,
                PaymentStatus.PAID)
            ;

        // When
        boolean actual = paymentRepository.existsByRentalUserIdAndStatusIn(userId, statuses);

        // Then
        assertThat(actual).isFalse();

    }

}
