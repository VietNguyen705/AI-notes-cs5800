package com.notesapp.services;

import com.notesapp.entities.Reminder;
import com.notesapp.entities.TodoItem;
import com.notesapp.enums.TaskStatus;
import com.notesapp.mediator.*;
import com.notesapp.repositories.ReminderRepository;
import com.notesapp.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MEDIATOR PATTERN Implementation
 *
 * This service has been refactored to use the Mediator pattern for notification delivery.
 * Instead of directly managing notification channels via switch statements, it delegates
 * to the NotificationMediator which coordinates all notification channels.
 *
 * Benefits:
 * - No switch statements - follows Open/Closed Principle
 * - Easy to add new notification channels without modifying this class
 * - Reduced coupling between scheduler and notification channels
 * - Centralized notification routing logic
 */
@Service
public class NotificationScheduler {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private TaskRepository taskRepository;

    // MEDIATOR PATTERN: Mediator and Colleague components
    @Autowired
    private NotificationMediator mediator;

    @Autowired
    private EmailNotificationChannel emailChannel;

    @Autowired
    private PushNotificationChannel pushChannel;

    @Autowired
    private SMSNotificationChannel smsChannel;

    @Autowired
    private InAppNotificationChannel inAppChannel;

    /**
     * MEDIATOR PATTERN: Register all notification channels with the mediator
     * This happens once when the service is initialized
     */
    @PostConstruct
    public void initialize() {
        mediator.registerChannel(emailChannel);
        mediator.registerChannel(pushChannel);
        mediator.registerChannel(smsChannel);
        mediator.registerChannel(inAppChannel);
        System.out.println("✓ NotificationScheduler initialized with " + mediator.getChannelCount() + " channels");
    }

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

    /**
     * MEDIATOR PATTERN: Delegates notification delivery to the mediator
     * The switch statement has been eliminated - the mediator handles channel routing
     */
    public void deliverNotification(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null");
        }

        if (reminder.getIsDelivered()) {
            throw new IllegalStateException("Reminder already delivered");
        }

        // MEDIATOR PATTERN: Delegate to mediator instead of using switch statement
        // The mediator will route to the appropriate channel based on reminder.getChannel()
        mediator.sendNotification(reminder);

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

    /*
     * MEDIATOR PATTERN: The old notification methods have been removed
     *
     * Previously, this class had sendPushNotification(), sendEmailNotification(),
     * sendSMSNotification(), and sendInAppNotification() methods.
     *
     * These have been replaced by NotificationChannel implementations:
     * - PushNotificationChannel
     * - EmailNotificationChannel
     * - SMSNotificationChannel
     * - InAppNotificationChannel
     *
     * All communication now goes through the NotificationMediator,
     * eliminating the switch statement and adhering to the Open/Closed Principle.
     */
}
