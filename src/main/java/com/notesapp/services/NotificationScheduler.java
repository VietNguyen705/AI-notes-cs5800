package com.notesapp.services;

import com.notesapp.entities.Reminder;
import com.notesapp.entities.TodoItem;
import com.notesapp.enums.TaskStatus;
import com.notesapp.repositories.ReminderRepository;
import com.notesapp.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationScheduler {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private TaskRepository taskRepository;

    public void scheduleReminder(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null");
        }

        if (reminder.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        reminderRepository.save(reminder);
    }

    public void cancelReminder(String reminderId) {
        if (reminderId == null) {
            throw new IllegalArgumentException("Reminder ID cannot be null");
        }

        Reminder reminder = reminderRepository.findById(reminderId)
            .orElseThrow(() -> new IllegalArgumentException("Reminder not found: " + reminderId));

        reminderRepository.delete(reminder);
    }

    public void deliverNotification(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null");
        }

        if (reminder.getIsDelivered()) {
            throw new IllegalStateException("Reminder already delivered");
        }

        // Simulate notification delivery based on channel
        String message = reminder.getMessage() != null ? reminder.getMessage() : "Reminder";

        switch (reminder.getChannel()) {
            case PUSH:
                sendPushNotification(message);
                break;
            case EMAIL:
                sendEmailNotification(message);
                break;
            case SMS:
                sendSMSNotification(message);
                break;
            case IN_APP:
                sendInAppNotification(message);
                break;
        }

        reminder.setIsDelivered(true);
        reminderRepository.save(reminder);
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkAndDeliverReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> pendingReminders = reminderRepository.findPendingReminders(now);

        for (Reminder reminder : pendingReminders) {
            try {
                deliverNotification(reminder);
            } catch (Exception e) {
                System.err.println("Failed to deliver reminder " + reminder.getReminderId() + ": " + e.getMessage());
            }
        }
    }

    public List<TodoItem> checkDueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findDueTasks(TaskStatus.PENDING, now);
    }

    @Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
    public void checkAndNotifyDueTasks() {
        List<TodoItem> dueTasks = checkDueTasks();

        for (TodoItem task : dueTasks) {
            if (task.getReminder() == null) {
                // Create a reminder for due tasks without one
                String message = "Task due: " + task.getTitle();
                System.out.println("IN_APP notification: " + message);
            }
        }
    }

    // Simulated notification methods
    private void sendPushNotification(String message) {
        System.out.println("PUSH notification sent: " + message);
        // In production: integrate with Firebase Cloud Messaging or similar
    }

    private void sendEmailNotification(String message) {
        System.out.println("EMAIL notification sent: " + message);
        // In production: integrate with email service (SendGrid, AWS SES, etc.)
    }

    private void sendSMSNotification(String message) {
        System.out.println("SMS notification sent: " + message);
        // In production: integrate with SMS service (Twilio, AWS SNS, etc.)
    }

    private void sendInAppNotification(String message) {
        System.out.println("IN-APP notification: " + message);
        // In production: use WebSockets or Server-Sent Events to push to frontend
    }
}
