package com.notesapp.mediator;

import org.springframework.stereotype.Component;

/**
 * Mediator Pattern - Concrete Colleague
 *
 * In-app notification channel implementation.
 * Displays notifications within the application interface.
 */
@Component
public class InAppNotificationChannel implements NotificationChannel {

    @Override
    public void send(String message, String recipient) {
        // In a real implementation, this would store the notification in the database
        // and send it via WebSocket to the user's active session
        System.out.println("🔔 [IN-APP] Notification for user " + recipient + ": " + message);

        // Placeholder for actual in-app notification logic
        // Example: webSocketService.sendToUser(recipient, message);
    }

    @Override
    public com.notesapp.enums.NotificationChannel getType() {
        return com.notesapp.enums.NotificationChannel.IN_APP;
    }
}
