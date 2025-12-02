package com.notesapp.factories;

import com.notesapp.enums.NotificationChannel;
import com.notesapp.mediator.*;
import org.springframework.stereotype.Component;

/**
 * Factory pattern implementation for creating notification channels.
 * Centralizes the creation logic for different notification channel types.
 */
@Component
public class NotificationChannelFactory {

    /**
     * Creates a notification channel based on the specified type.
     *
     * @param channelType The type of notification channel to create
     * @return A new instance of the specified notification channel
     * @throws IllegalArgumentException if channelType is null or unsupported
     */
    public com.notesapp.mediator.NotificationChannel createChannel(NotificationChannel channelType) {
        if (channelType == null) {
            throw new IllegalArgumentException("Channel type cannot be null");
        }

        switch (channelType) {
            case EMAIL:
                return new EmailNotificationChannel();
            case PUSH:
                return new PushNotificationChannel();
            case SMS:
                return new SMSNotificationChannel();
            case IN_APP:
                return new InAppNotificationChannel();
            default:
                throw new IllegalArgumentException("Unsupported channel type: " + channelType);
        }
    }

    /**
     * Creates a notification channel by string name (case-insensitive).
     *
     * @param channelName The name of the channel type (e.g., "email", "push", "sms", "in_app")
     * @return A new instance of the specified notification channel
     * @throws IllegalArgumentException if channelName is null, empty, or unsupported
     */
    public com.notesapp.mediator.NotificationChannel createChannelByName(String channelName) {
        if (channelName == null || channelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Channel name cannot be null or empty");
        }

        try {
            NotificationChannel channelType = NotificationChannel.valueOf(channelName.toUpperCase());
            return createChannel(channelType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown channel name: " + channelName, e);
        }
    }
}
