package com.notesapp.mediator;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public void send(String message, String recipient) {
        System.out.println("📧 [EMAIL] Sending to " + recipient + ": " + message);

    }

    @Override
    public com.notesapp.enums.NotificationChannel getType() {
        return com.notesapp.enums.NotificationChannel.EMAIL;
    }
}
