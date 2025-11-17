package com.notesapp.mediator;

import com.notesapp.entities.Reminder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationMediator {
    private final Map<com.notesapp.enums.NotificationChannel, NotificationChannel> channels;

    public NotificationMediator() {
        this.channels = new HashMap<>();
    }

    public void registerChannel(NotificationChannel channel) {
        channels.put(channel.getType(), channel);
        System.out.println("✓ Registered notification channel: " + channel.getType());
    }

    public void sendNotification(Reminder reminder) {
        NotificationChannel channel = channels.get(reminder.getChannel());

        if (channel != null) {
            String message = formatMessage(reminder);
            channel.send(message, reminder.getEntityId());
        } else {
            System.err.println("⚠ Warning: No channel registered for type " + reminder.getChannel());
        }
    }

    public void broadcast(String message, String recipient, List<com.notesapp.enums.NotificationChannel> channelTypes) {
        for (com.notesapp.enums.NotificationChannel type : channelTypes) {
            NotificationChannel channel = channels.get(type);
            if (channel != null) {
                channel.send(message, recipient);
            }
        }
    }

    private String formatMessage(Reminder reminder) {
        return String.format("⏰ Reminder: %s (scheduled for %s)",
                reminder.getMessage(),
                reminder.getScheduledTime());
    }

    public int getChannelCount() {
        return channels.size();
    }
}
