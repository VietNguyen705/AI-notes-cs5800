package com.notesapp.mediator;

import org.springframework.stereotype.Component;

@Component
public class InAppNotificationChannel implements NotificationChannel {

    @Override
    public void send(String message, String recipient) {
        System.out.println("🔔 [IN-APP] Notification for user " + recipient + ": " + message);

    }

    @Override
    public com.notesapp.enums.NotificationChannel getType() {
        return com.notesapp.enums.NotificationChannel.IN_APP;
    }
}
