package org.project.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app",
        name = "scheduling-enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
public class RentalScheduler {

    private final RentalService rentalService;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkOverdueRentals() {
        rentalService.sendOverdueRentalNotifications();
        log.info("Rental scheduler was triggered to check overdue rentals");
    }
}
