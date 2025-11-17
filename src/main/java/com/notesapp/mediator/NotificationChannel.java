package com.notesapp.mediator;

public interface NotificationChannel {
    void send(String message, String recipient);

    com.notesapp.enums.NotificationChannel getType();
}
