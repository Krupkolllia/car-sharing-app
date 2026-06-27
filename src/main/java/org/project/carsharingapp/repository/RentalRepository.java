package org.project.carsharingapp.repository;

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
            AND (:isActive IS NULL OR (r.actualReturnDate IS NULL) = :isActive)
            """)
    Page<Rental> findAllByFilters(
            @Param("userId") Long userId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"car"})
    @Query("SELECT r FROM Rental r WHERE r.id = :id")
    Optional<Rental> findByIdWithCar(@Param("id") Long id);

}
