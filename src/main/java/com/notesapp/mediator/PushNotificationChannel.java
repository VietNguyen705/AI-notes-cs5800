package com.notesapp.mediator;

import org.springframework.stereotype.Component;

@Component
public class PushNotificationChannel implements NotificationChannel {

    @Override
    public void send(String message, String recipient) {
        System.out.println("📱 [PUSH] Sending to device " + recipient + ": " + message);

    }

    @Override
    public com.notesapp.enums.NotificationChannel getType() {
        return com.notesapp.enums.NotificationChannel.PUSH;
    }
}
