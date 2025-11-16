package com.notesapp.mediator;

/**
 * Mediator Pattern - Colleague Interface
 *
 * Defines the interface for notification delivery channels.
 * Each channel implements its own delivery mechanism (email, push, SMS, etc.)
 * and communicates through the NotificationMediator.
 */
public interface NotificationChannel {
    /**
     * Sends a notification message through this channel
     * @param message the notification message
     * @param recipient the recipient identifier (user ID, email, phone, etc.)
     */
    void send(String message, String recipient);

    /**
     * Returns the type of this notification channel
     * @return the channel type (using fully qualified name to avoid naming conflict)
     */
    com.notesapp.enums.NotificationChannel getType();
}
