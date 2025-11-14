package com.notesapp.entities;

import com.notesapp.enums.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Reminder entity
 * Tests schedule, cancel, reschedule, and deliver methods
 */
@DisplayName("Reminder Entity Tests")
class ReminderTest {

    private Reminder reminder;

    @BeforeEach
    void setUp() {
        reminder = new Reminder();
        reminder.setReminderId("reminder-1");
        reminder.setEntityId("entity-1");
        reminder.setEntityType("NOTE");
        reminder.setScheduledTime(LocalDateTime.now().plusHours(1));
        reminder.setChannel(NotificationChannel.PUSH);
        reminder.setMessage("Test reminder");
        reminder.setIsDelivered(false);
    }

    // ==================== schedule() Tests ====================
    @Test
    @DisplayName("schedule() - Schedules valid future time successfully")
    void test_schedule_validFutureTime_succeeds() {
        // Act
        reminder.schedule();

        // Assert
        assertNotNull(reminder.getScheduledTime());
    }

    @Test
    @DisplayName("schedule() - Past time throws IllegalArgumentException")
    void test_schedule_pastTime_throwsException() {
        // Arrange
        reminder.setScheduledTime(LocalDateTime.now().minusHours(1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            reminder::schedule
        );
        assertEquals("Scheduled time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("schedule() - Null scheduled time throws IllegalArgumentException")
    void test_schedule_nullTime_throwsException() {
        // Arrange
        reminder.setScheduledTime(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            reminder::schedule
        );
        assertEquals("Scheduled time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("schedule() - Far future time is accepted")
    void test_schedule_farFutureTime_succeeds() {
        // Arrange
        reminder.setScheduledTime(LocalDateTime.now().plusYears(1));

        // Act
        reminder.schedule();

        // Assert
        assertNotNull(reminder.getScheduledTime());
    }

    // ==================== cancel() Tests ====================
    @Test
    @DisplayName("cancel() - Cancels scheduled reminder")
    void test_cancel_scheduledReminder_succeeds() {
        // Arrange
        reminder.setIsDelivered(true);

        // Act
        reminder.cancel();

        // Assert
        assertEquals(false, reminder.getIsDelivered());
    }

    @Test
    @DisplayName("cancel() - Sets isDelivered to false")
    void test_cancel_setsIsDeliveredFalse() {
        // Arrange
        reminder.setIsDelivered(true);

        // Act
        reminder.cancel();

        // Assert
        assertFalse(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("cancel() - Cancelling already cancelled reminder succeeds")
    void test_cancel_alreadyCancelled_succeeds() {
        // Arrange
        reminder.setIsDelivered(false);

        // Act
        reminder.cancel();

        // Assert
        assertFalse(reminder.getIsDelivered());
    }

    // ==================== reschedule() Tests ====================
    @Test
    @DisplayName("reschedule() - Reschedules to valid future time")
    void test_reschedule_validFutureTime_succeeds() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);

        // Act
        reminder.reschedule(newTime);

        // Assert
        assertEquals(newTime, reminder.getScheduledTime());
    }

    @Test
    @DisplayName("reschedule() - Sets isDelivered to false")
    void test_reschedule_setsIsDeliveredFalse() {
        // Arrange
        reminder.setIsDelivered(true);
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);

        // Act
        reminder.reschedule(newTime);

        // Assert
        assertFalse(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("reschedule() - Past time throws IllegalArgumentException")
    void test_reschedule_pastTime_throwsException() {
        // Arrange
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reminder.reschedule(pastTime)
        );
        assertEquals("New time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("reschedule() - Null time throws IllegalArgumentException")
    void test_reschedule_nullTime_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reminder.reschedule(null)
        );
        assertEquals("New time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("reschedule() - Updates scheduled time correctly")
    void test_reschedule_updatesScheduledTime() {
        // Arrange
        LocalDateTime originalTime = reminder.getScheduledTime();
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);

        // Act
        reminder.reschedule(newTime);

        // Assert
        assertNotEquals(originalTime, reminder.getScheduledTime());
        assertEquals(newTime, reminder.getScheduledTime());
    }

    // ==================== deliver() Tests ====================
    @Test
    @DisplayName("deliver() - Marks scheduled reminder as delivered")
    void test_deliver_scheduledReminder_marksDelivered() {
        // Arrange
        assertFalse(reminder.getIsDelivered());

        // Act
        reminder.deliver();

        // Assert
        assertTrue(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("deliver() - Already delivered throws IllegalStateException")
    void test_deliver_alreadyDelivered_throwsException() {
        // Arrange
        reminder.setIsDelivered(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            reminder::deliver
        );
        assertEquals("Reminder already delivered", exception.getMessage());
    }

    @Test
    @DisplayName("deliver() - Sets isDelivered to true")
    void test_deliver_setsIsDeliveredTrue() {
        // Act
        reminder.deliver();

        // Assert
        assertTrue(reminder.getIsDelivered());
    }

    // ==================== Entity Properties Tests ====================
    @Test
    @DisplayName("Reminder - Properties are mutable")
    void test_reminder_propertiesAreMutable() {
        // Act
        reminder.setMessage("Updated message");
        reminder.setChannel(NotificationChannel.EMAIL);
        reminder.setEntityType("TASK");

        // Assert
        assertEquals("Updated message", reminder.getMessage());
        assertEquals(NotificationChannel.EMAIL, reminder.getChannel());
        assertEquals("TASK", reminder.getEntityType());
    }

    @Test
    @DisplayName("Reminder - Can be associated with Note")
    void test_reminder_associatedWithNote() {
        // Arrange
        Note note = new Note();
        note.setNoteId("note-1");

        // Act
        reminder.setNote(note);

        // Assert
        assertEquals(note, reminder.getNote());
    }

    @Test
    @DisplayName("Reminder - Can be associated with TodoItem")
    void test_reminder_associatedWithTodoItem() {
        // Arrange
        TodoItem task = new TodoItem();
        task.setTaskId("task-1");

        // Act
        reminder.setTodoItem(task);

        // Assert
        assertEquals(task, reminder.getTodoItem());
    }

    @Test
    @DisplayName("Reminder - Default isDelivered is false")
    void test_reminder_defaultIsDeliveredFalse() {
        // Arrange
        Reminder newReminder = new Reminder();

        // Assert
        assertFalse(newReminder.getIsDelivered());
    }

    @Test
    @DisplayName("Reminder - Supports all notification channels")
    void test_reminder_supportsAllChannels() {
        // Act & Assert
        reminder.setChannel(NotificationChannel.PUSH);
        assertEquals(NotificationChannel.PUSH, reminder.getChannel());

        reminder.setChannel(NotificationChannel.EMAIL);
        assertEquals(NotificationChannel.EMAIL, reminder.getChannel());

        reminder.setChannel(NotificationChannel.SMS);
        assertEquals(NotificationChannel.SMS, reminder.getChannel());

        reminder.setChannel(NotificationChannel.IN_APP);
        assertEquals(NotificationChannel.IN_APP, reminder.getChannel());
    }
}
