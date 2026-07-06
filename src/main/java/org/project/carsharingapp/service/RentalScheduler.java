package org.project.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalScheduler {

    private final RentalService rentalService;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkOverdueRentals() {
        rentalService.sendOverdueRentalNotifications();
    }
}
