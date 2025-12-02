package com.notesapp.services;

import com.notesapp.entities.Reminder;
import com.notesapp.entities.TodoItem;
import com.notesapp.enums.TaskStatus;
import com.notesapp.mediator.*;
import com.notesapp.repositories.ReminderRepository;
import com.notesapp.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Singleton service for notification scheduling.
 * Spring's @Service annotation ensures only one instance exists (Singleton pattern).
 * Manages all reminders and scheduled tasks using the Mediator pattern for notification delivery.
 */
@Slf4j
@Service
public class NotificationScheduler {

  @Autowired
  private ReminderRepository reminderRepository;

  @Autowired
  private TaskRepository taskRepository;

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
   * Initializes the notification scheduler by registering all notification channels.
   * Called automatically after bean construction.
   */
  @PostConstruct
  public void initialize() {
    mediator.registerChannel(emailChannel);
    mediator.registerChannel(pushChannel);
    mediator.registerChannel(smsChannel);
    mediator.registerChannel(inAppChannel);

    int channelCount = mediator.getChannelCount();
    log.info("NotificationScheduler initialized with {} notification channels", channelCount);
  }

  /**
   * Schedules a reminder for future delivery.
   *
   * @param reminder the reminder to schedule
   * @throws IllegalArgumentException if reminder is null or scheduled time is in the past
   */
  public void scheduleReminder(Reminder reminder) {
    if (reminder == null) {
      log.warn("Attempted to schedule null reminder");
      throw new IllegalArgumentException("Reminder cannot be null");
    }

    if (reminder.getScheduledTime().isBefore(LocalDateTime.now())) {
      log.warn("Attempted to schedule reminder with past time: {}",
               reminder.getScheduledTime());
      throw new IllegalArgumentException("Scheduled time must be in the future");
    }

    reminderRepository.save(reminder);
    log.info("Scheduled reminder {} for {}",
             reminder.getReminderId(),
             reminder.getScheduledTime());
  }

  /**
   * Cancels a scheduled reminder.
   *
   * @param reminderId the ID of the reminder to cancel
   * @throws IllegalArgumentException if reminder ID is null or reminder not found
   */
  public void cancelReminder(String reminderId) {
    if (reminderId == null) {
      log.warn("Attempted to cancel reminder with null ID");
      throw new IllegalArgumentException("Reminder ID cannot be null");
    }

    Reminder reminder = reminderRepository.findById(reminderId)
        .orElseThrow(() -> {
          log.warn("Reminder not found: {}", reminderId);
          return new IllegalArgumentException("Reminder not found: " + reminderId);
        });

    reminderRepository.delete(reminder);
    log.info("Cancelled reminder {}", reminderId);
  }

  /**
   * Delivers a notification for the given reminder using the mediator.
   *
   * @param reminder the reminder to deliver
   * @throws IllegalArgumentException if reminder is null
   * @throws IllegalStateException if reminder was already delivered
   */
  public void deliverNotification(Reminder reminder) {
    if (reminder == null) {
      log.warn("Attempted to deliver null reminder");
      throw new IllegalArgumentException("Reminder cannot be null");
    }

    if (reminder.getIsDelivered()) {
      log.warn("Attempted to deliver already-delivered reminder: {}",
               reminder.getReminderId());
      throw new IllegalStateException("Reminder already delivered");
    }

    mediator.sendNotification(reminder);
    reminder.setIsDelivered(true);
    reminderRepository.save(reminder);

    log.info("Delivered reminder {} via {} channel",
             reminder.getReminderId(),
             reminder.getChannel());
  }

  /**
   * Checks for pending reminders and delivers them.
   * Runs every 60 seconds (60000ms).
   */
  @Scheduled(fixedRate = 60000)
  public void checkAndDeliverReminders() {
    LocalDateTime now = LocalDateTime.now();
    List<Reminder> pendingReminders = reminderRepository.findPendingReminders(now);

    if (!pendingReminders.isEmpty()) {
      log.debug("Found {} pending reminders to deliver", pendingReminders.size());
    }

    for (Reminder reminder : pendingReminders) {
      try {
        deliverNotification(reminder);
      } catch (Exception e) {
        log.error("Failed to deliver reminder {}: {}",
                  reminder.getReminderId(),
                  e.getMessage(),
                  e);
      }
    }
  }

  /**
   * Retrieves all tasks that are due or overdue.
   *
   * @return list of due tasks with PENDING status
   */
  public List<TodoItem> checkDueTasks() {
    LocalDateTime now = LocalDateTime.now();
    List<TodoItem> dueTasks = taskRepository.findDueTasks(TaskStatus.PENDING, now);

    if (!dueTasks.isEmpty()) {
      log.debug("Found {} due tasks", dueTasks.size());
    }

    return dueTasks;
  }

  /**
   * Checks for due tasks and sends notifications.
   * Runs daily at 9:00 AM (cron: "0 0 9 * * *").
   */
  @Scheduled(cron = "0 0 9 * * *")
  public void checkAndNotifyDueTasks() {
    List<TodoItem> dueTasks = checkDueTasks();

    if (dueTasks.isEmpty()) {
      log.debug("No due tasks found for daily notification");
      return;
    }

    log.info("Sending notifications for {} due tasks", dueTasks.size());

    for (TodoItem task : dueTasks) {
      if (task.getReminder() == null) {
        String message = "Task due: " + task.getTitle();
        log.info("IN_APP notification: {}", message);
      }
    }
  }
}
