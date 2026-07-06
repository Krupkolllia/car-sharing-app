package org.project.carsharingapp.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.mapper.RentalMapper;
import org.project.carsharingapp.model.rental.Rental;
import org.project.carsharingapp.repository.RentalRepository;
import org.project.carsharingapp.telegram.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalScheduler {

    private static final String NO_OVERDUE_RENTALS_MESSAGE = "No overdue rentals today!";

    private final RentalRepository rentalRepository;

    private final RentalMapper rentalMapper;

    private final NotificationService notificationService;

    private final Clock clock;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkOverdueRentals() {
        List<Rental> overdueRentals =
                rentalRepository.findAllOverdue(LocalDate.now(clock));

        if (overdueRentals.isEmpty()) {
            notificationService.sendNotification(NO_OVERDUE_RENTALS_MESSAGE);
            return;
        }

        overdueRentals.forEach(rental ->
                notificationService.sendNotification(
                    MessageBuilder.buildOverdueRentalMessage(rentalMapper.toMessageDto(rental))));
    }
}
