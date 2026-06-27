package org.project.carsharingapp.util;

import org.project.carsharingapp.model.car.Car;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.model.user.User;

public final class MessageBuilder {

    private MessageBuilder() {
        
    }

    public static String buildRentalCreatedMessage(Rental rental) {
        Car car = rental.getCar();
        User user = rental.getUser();

        return """
        New rental created
        
        Rental ID: %d
        
        Customer:
        ID: %d
        Email: %s
        
        Car:
        Brand: %s
        Model: %s
        
        Inventory: %d
        """.formatted(
            rental.getId(),
            user.getId(),
            user.getEmail(),
            car.getBrand(),
            car.getModel(),
            car.getInventory()
        );
    }

}
