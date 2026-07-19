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
        try {
            rentalService.sendOverdueRentalNotifications();
            log.info("Overdue rentals were successfully processed");
        } catch (Exception e) {
            log.error("Failed to process overdue rentals", e);
        }
    }

}
