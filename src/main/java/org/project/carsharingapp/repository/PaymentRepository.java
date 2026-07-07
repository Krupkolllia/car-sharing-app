package org.project.carsharingapp.repository;

import org.project.carsharingapp.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
