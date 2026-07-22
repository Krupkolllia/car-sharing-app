package org.project.carsharingapp.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.project.carsharingapp.dto.payment.rental.RentalPaymentMessageDto;
import org.project.carsharingapp.model.payment.Payment;
import org.project.carsharingapp.model.payment.PaymentStatus;
import org.project.carsharingapp.model.payment.PaymentType;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.User;

public class RentalPaymentMapperTest {

    private final RentalPaymentMapper rentalPaymentMapper =
            Mappers.getMapper(RentalPaymentMapper.class);

    @Test
    @DisplayName("""
        toMessageDto method with valid entity should
        map all fields correctly
        """)
    void toMessageDto_WithValidEntity_ShouldMapAllFieldsCorrectly() {
        // Given
        User user = new User()
            .setId(1L)
            .setEmail("john.doe@gmail.com")
            .setFirstName("John")
            .setLastName("Doe");

        Rental rental = new Rental()
            .setId(2L)
            .setUser(user);

        Payment payment = new Payment()
            .setId(3L)
            .setType(PaymentType.PAYMENT)
            .setStatus(PaymentStatus.PAID)
            .setRental(rental);

        // When
        RentalPaymentMessageDto actual = rentalPaymentMapper.toMessageDto(payment);

        // Then
        assertThat(actual).isNotNull();

        assertThat(actual.paymentId()).isEqualTo(3L);
        assertThat(actual.paymentType()).isEqualTo(PaymentType.PAYMENT);
        assertThat(actual.paymentStatus()).isEqualTo(PaymentStatus.PAID);

        assertThat(actual.userId()).isEqualTo(1L);
        assertThat(actual.userEmail()).isEqualTo("john.doe@gmail.com");

        assertThat(actual.rentalId()).isEqualTo(2L);
    }



}
