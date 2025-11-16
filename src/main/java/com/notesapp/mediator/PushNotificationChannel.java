package com.notesapp.mediator;

import org.springframework.stereotype.Component;

/**
 * Mediator Pattern - Concrete Colleague
 *
 * Push notification channel implementation.
 * Sends push notifications to mobile devices or web browsers.
 */
@Component
public class PushNotificationChannel implements NotificationChannel {

    @Override
    public void send(String message, String recipient) {
        // In a real implementation, this would integrate with a push service (e.g., Firebase Cloud Messaging, Apple Push Notification Service)
        System.out.println("📱 [PUSH] Sending to device " + recipient + ": " + message);

        // Placeholder for actual push notification logic
        // Example: fcmService.sendPushNotification(recipient, "Reminder", message);
    }

    @Override
    public com.notesapp.enums.NotificationChannel getType() {
        return com.notesapp.enums.NotificationChannel.PUSH;
    }
}
