package org.project.carsharingapp.util;

import org.project.carsharingapp.dto.payment.rental.RentalPaymentMessageDto;
import org.project.carsharingapp.dto.rental.RentalMessageDto;

public final class MessageBuilder {

    private MessageBuilder() {

    }

    public static String buildRentalCreatedMessage(RentalMessageDto rental) {
        return """
            New rental created

            Rental:
            * ID: %d
            * Rental date: %s
            * Return date: %s

            Customer:
            * ID: %d
            * Email: %s
            * Full name: %s %s

            Car:
            * ID: %d
            * Brand: %s
            * Model: %s
            * Type: %s
            * Daily fee: $%s
            * Available cars: %d
            """.formatted(
            rental.rentalId(),
            rental.rentalDate(),
            rental.returnDate(),
            rental.customerId(),
            rental.customerEmail(),
            rental.customerFirstName(),
            rental.customerLastName(),
            rental.carId(),
            rental.carBrand(),
            rental.carModel(),
            rental.carType(),
            rental.dailyFee(),
            rental.availableCars()
        );
    }

    public static String buildOverdueRentalMessage(RentalMessageDto rental) {
        return """
            Rental is overdue

            Rental:
            * ID: %d
            * Rental date: %s
            * Return date: %s

            Customer:
            * ID: %d
            * Email: %s
            * Full name: %s %s

            Car:
            * ID: %d
            * Brand: %s
            * Model: %s
            * Type: %s
            * Daily fee: $%s
            """.formatted(
            rental.rentalId(),
            rental.rentalDate(),
            rental.returnDate(),
            rental.customerId(),
            rental.customerEmail(),
            rental.customerFirstName(),
            rental.customerLastName(),
            rental.carId(),
            rental.carBrand(),
            rental.carModel(),
            rental.carType(),
            rental.dailyFee());
    }

    public static String buildRentalPaymentCompletedMessage(RentalPaymentMessageDto payment) {
        return """
            ✅ Payment completed

            Payment ID: %d
            Type: %s
            Amount: $%s
            User: %s
            User ID: %d
            Rental ID: %d
            Status: %s
            """.formatted(
                payment.paymentId(),
                payment.paymentType(),
                payment.total(),
                payment.userEmail(),
                payment.userId(),
                payment.rentalId(),
                payment.paymentStatus()
            );
    }
}
