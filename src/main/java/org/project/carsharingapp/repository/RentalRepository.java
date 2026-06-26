package org.project.carsharingapp.repository;

import org.project.carsharingapp.model.rental.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {

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

    @EntityGraph(attributePaths = {"car", "user"})
    @Query("SELECT r FROM Rental r")
    Page<Rental> findAllWithDetails(Pageable pageable);

}
