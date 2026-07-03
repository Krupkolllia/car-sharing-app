package org.project.carsharingapp.repository;

import org.project.carsharingapp.model.car.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarRepository extends JpaRepository<Car, Long> {

    @Modifying
    @Query("""
            UPDATE Car c
            SET c.inventory = c.inventory + 1
            WHERE c.id = :carId
            """)
    int increaseInventory(@Param("carId") Long carId);

    @Modifying
    @Query("""
            UPDATE Car c
            SET c.inventory = c.inventory - 1
            WHERE c.id = :carId
            AND c.inventory > 0
            """)
    int decreaseInventory(@Param("carId") Long carId);

}
