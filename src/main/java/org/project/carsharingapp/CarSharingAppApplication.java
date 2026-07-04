package org.project.carsharingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class CarSharingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarSharingAppApplication.class, args);
    }

}
