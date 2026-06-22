package org.project.carsharingapp.repository;

import org.project.carsharingapp.model.car.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {}
