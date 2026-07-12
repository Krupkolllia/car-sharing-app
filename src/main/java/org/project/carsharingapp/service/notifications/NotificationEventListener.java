package org.project.carsharingapp.service.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async("externalApiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationRequestedEvent event) {
        try {
            notificationService.sendNotification(event.message());
        } catch (Exception e) {
            System.err.println("Failed to send notification: \n" + event.message()
                    + "\n" + e.getMessage());
        }
    }

}
