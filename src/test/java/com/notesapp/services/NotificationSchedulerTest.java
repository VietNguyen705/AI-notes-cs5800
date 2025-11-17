package com.notesapp.services;

import com.notesapp.entities.Reminder;
import com.notesapp.entities.TodoItem;
import com.notesapp.enums.NotificationChannel;
import com.notesapp.enums.TaskStatus;
import com.notesapp.repositories.ReminderRepository;
import com.notesapp.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationScheduler Service Tests")
class NotificationSchedulerTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    private Reminder testReminder;
    private TodoItem testTask;

    @BeforeEach
    void setUp() {
        testReminder = new Reminder();
        testReminder.setReminderId("reminder-1");
        testReminder.setEntityId("note-1");
        testReminder.setEntityType("NOTE");
        testReminder.setScheduledTime(LocalDateTime.now().plusHours(1));
        testReminder.setChannel(NotificationChannel.PUSH);
        testReminder.setMessage("Test reminder");
        testReminder.setIsDelivered(false);

        testTask = new TodoItem();
        testTask.setTaskId("task-1");
        testTask.setTitle("Test Task");
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setDueDate(LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("scheduleReminder() - Null reminder throws IllegalArgumentException")
    void test_scheduleReminder_nullReminder_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationScheduler.scheduleReminder(null)
        );
        assertEquals("Reminder cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("scheduleReminder() - Past scheduled time throws IllegalArgumentException")
    void test_scheduleReminder_pastTime_throwsException() {
        testReminder.setScheduledTime(LocalDateTime.now().minusHours(1));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationScheduler.scheduleReminder(testReminder)
        );
        assertEquals("Scheduled time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("scheduleReminder() - Valid future time schedules successfully")
    void test_scheduleReminder_futureTime_succeeds() {
        when(reminderRepository.save(testReminder)).thenReturn(testReminder);

        notificationScheduler.scheduleReminder(testReminder);

        verify(reminderRepository).save(testReminder);
    }

    @Test
    @DisplayName("scheduleReminder() - Saves reminder to repository")
    void test_scheduleReminder_savesToRepository() {
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.scheduleReminder(testReminder);

        verify(reminderRepository, times(1)).save(testReminder);
    }

    @Test
    @DisplayName("scheduleReminder() - Far future time is accepted")
    void test_scheduleReminder_farFuture_succeeds() {
        testReminder.setScheduledTime(LocalDateTime.now().plusYears(1));
        when(reminderRepository.save(testReminder)).thenReturn(testReminder);

        notificationScheduler.scheduleReminder(testReminder);

        verify(reminderRepository).save(testReminder);
    }

    @Test
    @DisplayName("cancelReminder() - Null reminder ID throws IllegalArgumentException")
    void test_cancelReminder_nullId_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationScheduler.cancelReminder(null)
        );
        assertEquals("Reminder ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("cancelReminder() - Non-existent reminder throws IllegalArgumentException")
    void test_cancelReminder_nonExistentId_throwsException() {
        when(reminderRepository.findById("nonexistent"))
            .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationScheduler.cancelReminder("nonexistent")
        );
        assertEquals("Reminder not found: nonexistent", exception.getMessage());
    }

    @Test
    @DisplayName("cancelReminder() - Valid ID deletes reminder")
    void test_cancelReminder_validId_deletesReminder() {
        when(reminderRepository.findById("reminder-1"))
            .thenReturn(Optional.of(testReminder));

        notificationScheduler.cancelReminder("reminder-1");

        verify(reminderRepository).delete(testReminder);
    }

    @Test
    @DisplayName("cancelReminder() - Finds reminder by ID before deleting")
    void test_cancelReminder_findsBeforeDelete() {
        when(reminderRepository.findById("reminder-1"))
            .thenReturn(Optional.of(testReminder));

        notificationScheduler.cancelReminder("reminder-1");

        verify(reminderRepository).findById("reminder-1");
        verify(reminderRepository).delete(testReminder);
    }

    @Test
    @DisplayName("deliverNotification() - Null reminder throws IllegalArgumentException")
    void test_deliverNotification_nullReminder_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationScheduler.deliverNotification(null)
        );
        assertEquals("Reminder cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("deliverNotification() - Already delivered throws IllegalStateException")
    void test_deliverNotification_alreadyDelivered_throwsException() {
        testReminder.setIsDelivered(true);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> notificationScheduler.deliverNotification(testReminder)
        );
        assertEquals("Reminder already delivered", exception.getMessage());
    }

    @Test
    @DisplayName("deliverNotification() - Marks reminder as delivered")
    void test_deliverNotification_marksAsDelivered() {
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.deliverNotification(testReminder);

        assertTrue(testReminder.getIsDelivered());
        verify(reminderRepository).save(testReminder);
    }

    @Test
    @DisplayName("deliverNotification() - PUSH channel delivers notification")
    void test_deliverNotification_pushChannel_delivers() {
        testReminder.setChannel(NotificationChannel.PUSH);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.deliverNotification(testReminder);

        assertTrue(testReminder.getIsDelivered());
    }

    @Test
    @DisplayName("deliverNotification() - EMAIL channel delivers notification")
    void test_deliverNotification_emailChannel_delivers() {
        testReminder.setChannel(NotificationChannel.EMAIL);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.deliverNotification(testReminder);

        assertTrue(testReminder.getIsDelivered());
    }

    @Test
    @DisplayName("deliverNotification() - SMS channel delivers notification")
    void test_deliverNotification_smsChannel_delivers() {
        testReminder.setChannel(NotificationChannel.SMS);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.deliverNotification(testReminder);

        assertTrue(testReminder.getIsDelivered());
    }

    @Test
    @DisplayName("deliverNotification() - IN_APP channel delivers notification")
    void test_deliverNotification_inAppChannel_delivers() {
        testReminder.setChannel(NotificationChannel.IN_APP);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.deliverNotification(testReminder);

        assertTrue(testReminder.getIsDelivered());
    }

    @Test
    @DisplayName("deliverNotification() - Saves reminder after delivery")
    void test_deliverNotification_savesAfterDelivery() {
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.deliverNotification(testReminder);

        verify(reminderRepository).save(testReminder);
    }

    @Test
    @DisplayName("deliverNotification() - Null message uses default")
    void test_deliverNotification_nullMessage_usesDefault() {
        testReminder.setMessage(null);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.deliverNotification(testReminder);

        assertTrue(testReminder.getIsDelivered());
    }

    @Test
    @DisplayName("checkDueTasks() - Returns due pending tasks")
    void test_checkDueTasks_returnsDueTasks() {
        List<TodoItem> dueTasks = Arrays.asList(testTask);
        when(taskRepository.findDueTasks(eq(TaskStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(dueTasks);

        List<TodoItem> results = notificationScheduler.checkDueTasks();

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(taskRepository).findDueTasks(eq(TaskStatus.PENDING), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("checkDueTasks() - Returns empty list when no due tasks")
    void test_checkDueTasks_noDueTasks_returnsEmptyList() {
        when(taskRepository.findDueTasks(eq(TaskStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        List<TodoItem> results = notificationScheduler.checkDueTasks();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("checkDueTasks() - Uses current time for query")
    void test_checkDueTasks_usesCurrentTime() {
        when(taskRepository.findDueTasks(eq(TaskStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        notificationScheduler.checkDueTasks();

        verify(taskRepository).findDueTasks(eq(TaskStatus.PENDING), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("checkDueTasks() - Only checks PENDING status")
    void test_checkDueTasks_onlyChecksPending() {
        when(taskRepository.findDueTasks(eq(TaskStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        notificationScheduler.checkDueTasks();

        verify(taskRepository).findDueTasks(eq(TaskStatus.PENDING), any(LocalDateTime.class));
        verify(taskRepository, never()).findDueTasks(eq(TaskStatus.COMPLETED), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("checkAndDeliverReminders() - Delivers pending reminders")
    void test_checkAndDeliverReminders_deliversPending() {
        List<Reminder> pendingReminders = Arrays.asList(testReminder);
        when(reminderRepository.findPendingReminders(any(LocalDateTime.class)))
            .thenReturn(pendingReminders);
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);

        notificationScheduler.checkAndDeliverReminders();

        verify(reminderRepository).findPendingReminders(any(LocalDateTime.class));
        assertTrue(testReminder.getIsDelivered());
    }

    @Test
    @DisplayName("checkAndDeliverReminders() - Handles empty reminder list")
    void test_checkAndDeliverReminders_emptyList_noErrors() {
        when(reminderRepository.findPendingReminders(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        notificationScheduler.checkAndDeliverReminders();

        verify(reminderRepository).findPendingReminders(any(LocalDateTime.class));
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("checkAndDeliverReminders() - Continues on delivery error")
    void test_checkAndDeliverReminders_handleErrors_continues() {
        Reminder failingReminder = new Reminder();
        failingReminder.setReminderId("reminder-2");
        failingReminder.setIsDelivered(false);
        failingReminder.setChannel(NotificationChannel.EMAIL);

        List<Reminder> reminders = Arrays.asList(testReminder, failingReminder);
        when(reminderRepository.findPendingReminders(any(LocalDateTime.class)))
            .thenReturn(reminders);
        when(reminderRepository.save(any(Reminder.class)))
            .thenReturn(testReminder)
            .thenThrow(new RuntimeException("Delivery failed"));

        notificationScheduler.checkAndDeliverReminders();

        verify(reminderRepository).findPendingReminders(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("checkAndDeliverReminders() - Processes multiple reminders")
    void test_checkAndDeliverReminders_multipleReminders_processesAll() {
        Reminder reminder2 = new Reminder();
        reminder2.setReminderId("reminder-2");
        reminder2.setIsDelivered(false);
        reminder2.setChannel(NotificationChannel.EMAIL);
        reminder2.setMessage("Second reminder");

        List<Reminder> reminders = Arrays.asList(testReminder, reminder2);
        when(reminderRepository.findPendingReminders(any(LocalDateTime.class)))
            .thenReturn(reminders);
        when(reminderRepository.save(any(Reminder.class)))
            .thenReturn(testReminder, reminder2);

        notificationScheduler.checkAndDeliverReminders();

        verify(reminderRepository, times(2)).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Integration - Schedule and deliver reminder workflow")
    void test_integration_scheduleAndDeliver() {
        Reminder newReminder = new Reminder();
        newReminder.setReminderId("reminder-new");
        newReminder.setScheduledTime(LocalDateTime.now().plusSeconds(1));
        newReminder.setChannel(NotificationChannel.PUSH);
        newReminder.setMessage("Integration test");
        newReminder.setIsDelivered(false);

        when(reminderRepository.save(any(Reminder.class))).thenReturn(newReminder);

        notificationScheduler.scheduleReminder(newReminder);

        verify(reminderRepository, times(1)).save(newReminder);

        notificationScheduler.deliverNotification(newReminder);

        assertTrue(newReminder.getIsDelivered());
        verify(reminderRepository, times(2)).save(newReminder);
    }

    @Test
    @DisplayName("Integration - Schedule and cancel reminder workflow")
    void test_integration_scheduleAndCancel() {
        when(reminderRepository.save(any(Reminder.class))).thenReturn(testReminder);
        when(reminderRepository.findById("reminder-1")).thenReturn(Optional.of(testReminder));

        notificationScheduler.scheduleReminder(testReminder);

        verify(reminderRepository).save(testReminder);

        notificationScheduler.cancelReminder("reminder-1");

        verify(reminderRepository).delete(testReminder);
    }
}
