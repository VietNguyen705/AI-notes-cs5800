package com.notesapp.mediator;

import com.notesapp.entities.Reminder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mediator Pattern - Mediator
 *
 * Central coordinator for all notification channels.
 * This mediator eliminates direct dependencies between notification channels
 * and the notification scheduler. Channels register with the mediator and
 * the mediator routes notifications to the appropriate channel(s).
 *
 * Benefits:
 * - Reduces coupling between components
 * - Easy to add new notification channels without modifying existing code
 * - Centralizes notification routing logic
 * - Supports broadcasting to multiple channels
 */
@Component
public class NotificationMediator {
    private final Map<com.notesapp.enums.NotificationChannel, NotificationChannel> channels;

    public NotificationMediator() {
        this.channels = new HashMap<>();
    }

    /**
     * Registers a notification channel with the mediator
     * @param channel the channel to register
     */
    public void registerChannel(NotificationChannel channel) {
        channels.put(channel.getType(), channel);
        System.out.println("✓ Registered notification channel: " + channel.getType());
    }

    /**
     * Sends a notification through the specified channel
     * @param reminder the reminder containing the message and channel type
     */
    public void sendNotification(Reminder reminder) {
        NotificationChannel channel = channels.get(reminder.getChannel());

        if (channel != null) {
            String message = formatMessage(reminder);
            // Use entityId as recipient identifier (could be userId, noteId, or taskId)
            channel.send(message, reminder.getEntityId());
        } else {
            System.err.println("⚠ Warning: No channel registered for type " + reminder.getChannel());
        }
    }

    /**
     * Broadcasts a message to multiple channels
     * @param message the message to broadcast
     * @param recipient the recipient identifier
     * @param channelTypes list of channel types to broadcast to
     */
    public void broadcast(String message, String recipient, List<com.notesapp.enums.NotificationChannel> channelTypes) {
        for (com.notesapp.enums.NotificationChannel type : channelTypes) {
            NotificationChannel channel = channels.get(type);
            if (channel != null) {
                channel.send(message, recipient);
            }
        }
    }

    /**
     * Formats the reminder into a user-friendly message
     * @param reminder the reminder to format
     * @return formatted message string
     */
    private String formatMessage(Reminder reminder) {
        return String.format("⏰ Reminder: %s (scheduled for %s)",
                reminder.getMessage(),
                reminder.getScheduledTime());
    }

    /**
     * Gets the count of registered channels
     * @return number of registered channels
     */
    public int getChannelCount() {
        return channels.size();
    }
}
