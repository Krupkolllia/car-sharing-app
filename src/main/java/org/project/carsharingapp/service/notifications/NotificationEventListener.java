package org.project.carsharingapp.service.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(
        prefix = "telegram",
        name = "notifications-enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async("externalApiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationRequestedEvent event) {
        try {
            notificationService.sendNotification(event.message());
            log.debug("NotificationEventListener was triggered");
        } catch (Exception e) {
            log.warn("Failed to send notification", e);
        }
    }

}
