package com.notesapp.services;

import com.notesapp.entities.Reminder;
import com.notesapp.entities.TodoItem;
import com.notesapp.enums.TaskStatus;
import com.notesapp.mediator.*;
import com.notesapp.repositories.ReminderRepository;
import com.notesapp.repositories.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Singleton pattern implementation for notification scheduling.
 * Ensures only one instance manages all reminders and scheduled tasks.
 */
@Service
public class NotificationScheduler {

    private static NotificationScheduler instance;

    private ReminderRepository reminderRepository;
    private TaskRepository taskRepository;
    private NotificationMediator mediator;
    private EmailNotificationChannel emailChannel;
    private PushNotificationChannel pushChannel;
    private SMSNotificationChannel smsChannel;
    private InAppNotificationChannel inAppChannel;

    private NotificationScheduler() {
    }

    public static synchronized NotificationScheduler getInstance() {
        if (instance == null) {
            instance = new NotificationScheduler();
        }
        return instance;
    }

    public void setDependencies(ReminderRepository reminderRepository,
                                TaskRepository taskRepository,
                                NotificationMediator mediator,
                                EmailNotificationChannel emailChannel,
                                PushNotificationChannel pushChannel,
                                SMSNotificationChannel smsChannel,
                                InAppNotificationChannel inAppChannel) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
        this.mediator = mediator;
        this.emailChannel = emailChannel;
        this.pushChannel = pushChannel;
        this.smsChannel = smsChannel;
        this.inAppChannel = inAppChannel;
    }

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

    public void deliverNotification(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null");
        }

        if (reminder.getIsDelivered()) {
            throw new IllegalStateException("Reminder already delivered");
        }

        mediator.sendNotification(reminder);

        reminder.setIsDelivered(true);
        reminderRepository.save(reminder);
    }

    @Scheduled(fixedRate = 60000)
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

    @Scheduled(cron = "0 0 9 * * *")
    public void checkAndNotifyDueTasks() {
        List<TodoItem> dueTasks = checkDueTasks();

        for (TodoItem task : dueTasks) {
            if (task.getReminder() == null) {
                String message = "Task due: " + task.getTitle();
                System.out.println("IN_APP notification: " + message);
            }
        }
    }
}
