package org.project.carsharingapp.util;

import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.User;

public final class TelegramMessageBuilder {

    private TelegramMessageBuilder() {

    }

    public static String buildRentalCreatedMessage(Rental rental) {
        Car car = rental.getCar();
        User user = rental.getUser();

        return """
            New rental created
        
            Rental:
            * ID: %d
            * Rental date: %s
            * Return date: %s
        
            Customer:
            * ID: %d
            * First name: %s
            * Last name: %s
            * Email: %s
        
            Car:
            * ID: %d
            * Brand: %s
            * Model: %s
            * Type: %s
            * Daily fee: $%s
            * Available cars: %d
        """.formatted(
            rental.getId(),
            rental.getRentalDate(),
            rental.getReturnDate(),
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            car.getId(),
            car.getBrand(),
            car.getModel(),
            car.getType(),
            car.getDailyFee(),
            car.getInventory()
        );
    }

    public static String buildRentalReturnedMessage(Rental rental) {
        return """
            Rental returned

            Rental:
            * ID: %d
            * Rental date: %s
            * Return date: %s
            * Actual return date: %s

            Customer:
            * ID: %d
            * First name: %s
            * Last name: %s
            * Email: %s

            Car:
            * ID: %d
            * Brand: %s
            * Model: %s
            * Type: %s
            * Daily fee: $%s
            * Available cars: %d
            """.formatted(
            rental.getId(),
            rental.getRentalDate(),
            rental.getReturnDate(),
            rental.getActualReturnDate(),
            rental.getUser().getId(),
            rental.getUser().getFirstName(),
            rental.getUser().getLastName(),
            rental.getUser().getEmail(),
            rental.getCar().getId(),
            rental.getCar().getBrand(),
            rental.getCar().getModel(),
            rental.getCar().getType(),
            rental.getCar().getDailyFee(),
            rental.getCar().getInventory()
        );
    }

}
