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

}
