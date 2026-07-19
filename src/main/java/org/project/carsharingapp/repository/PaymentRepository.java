package org.project.carsharingapp.repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"rental"})
    @Query("""
            SELECT p FROM Payment p
            WHERE (:userId IS NULL OR p.rental.user.id = :userId)
            """)
    Page<Payment> findAllFilteredByUserId(@Param("userId") Long userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findAllByStatus(PaymentStatus status);

    @EntityGraph(attributePaths = {
        "rental",
        "rental.car",
        "rental.user"
    })
    Optional<Payment> findByIdAndRentalUserId(Long id, Long userId);

    boolean existsByRentalUserIdAndStatusIn(
            Long userId, Collection<PaymentStatus> statuses);

    boolean existsBySessionId(String sessionId);

}
