package org.project.carsharingapp.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.project.carsharingapp.model.rental.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    @EntityGraph(attributePaths = {"car"})
    @Query("""
            SELECT r FROM Rental r
            WHERE (:userId IS NULL OR r.user.id = :userId)
            AND (:isActive IS NULL
            OR (:isActive = TRUE AND r.actualReturnDate IS NULL)
            OR (:isActive = FALSE AND r.actualReturnDate IS NOT NULL))
            """)
    Page<Rental> findAllByFilters(
            @Param("userId") Long userId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"car"})
    @Query("SELECT r FROM Rental r WHERE r.id = :id")
    Optional<Rental> findByIdWithCar(@Param("id") Long id);

    @EntityGraph(attributePaths = {"car"})
    @Query("""
            SELECT r FROM Rental r
            WHERE r.actualReturnDate IS NULL
            AND r.returnDate <= :tomorrow
            """)
    List<Rental> findAllOverdue(@Param("tomorrow") LocalDate tomorrow);

}
