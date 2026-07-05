package org.project.carsharingapp.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.service.NotificationService;
import org.project.carsharingapp.service.RentalScheduler;
import org.project.carsharingapp.util.TelegramMessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalSchedulerImpl implements RentalScheduler {

    private final RentalRepository rentalRepository;

    private final NotificationService notificationService;

    private final Clock clock;

    @Scheduled(cron = "0 0 8 * * *")
    @Override
    public void checkOverdueRentals() {
        List<Rental> overdueRentals =
                rentalRepository.findAllOverdue(LocalDate.now(clock).plusDays(1));

        if (overdueRentals.isEmpty()) {
            notificationService.sendNotification("No rentals overdue today!");
            return;
        }

        overdueRentals.forEach(rental ->
                notificationService.sendNotification(
                    TelegramMessageBuilder.buildOverdueRentalMessage(rental)));
    }
}
