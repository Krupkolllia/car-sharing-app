package org.project.carsharingapp.service.impl;

import org.project.carsharingapp.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NoOpNotificationService implements NotificationService {

    @Override
    public void sendNotification(String message) {

    }
}
