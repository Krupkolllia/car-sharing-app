package org.project.carsharingapp.repository;

import org.project.carsharingapp.model.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"rental"})
    @Query("""
            SELECT p FROM Payment p
            WHERE (:userId IS NULL OR p.rental.user.id = :userId)
            """)
    Page<Payment> findAllFilteredByUserId(@Param("userId") Long userId, Pageable pageable);

}
