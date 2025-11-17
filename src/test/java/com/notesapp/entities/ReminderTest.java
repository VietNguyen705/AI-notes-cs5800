package com.notesapp.entities;

import com.notesapp.enums.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @DisplayName("schedule() - Schedules valid future time successfully")
    void test_schedule_validFutureTime_succeeds() {
        reminder.schedule();

        assertNotNull(reminder.getScheduledTime());
    }

    @Test
    @DisplayName("schedule() - Past time throws IllegalArgumentException")
    void test_schedule_pastTime_throwsException() {
        reminder.setScheduledTime(LocalDateTime.now().minusHours(1));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            reminder::schedule
        );
        assertEquals("Scheduled time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("schedule() - Null scheduled time throws IllegalArgumentException")
    void test_schedule_nullTime_throwsException() {
        reminder.setScheduledTime(null);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            reminder::schedule
        );
        assertEquals("Scheduled time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("schedule() - Far future time is accepted")
    void test_schedule_farFutureTime_succeeds() {
        reminder.setScheduledTime(LocalDateTime.now().plusYears(1));

        reminder.schedule();

        assertNotNull(reminder.getScheduledTime());
    }

    @Test
    @DisplayName("cancel() - Cancels scheduled reminder")
    void test_cancel_scheduledReminder_succeeds() {
        reminder.setIsDelivered(true);

        reminder.cancel();

        assertEquals(false, reminder.getIsDelivered());
    }

    @Test
    @DisplayName("cancel() - Sets isDelivered to false")
    void test_cancel_setsIsDeliveredFalse() {
        reminder.setIsDelivered(true);

        reminder.cancel();

        assertFalse(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("cancel() - Cancelling already cancelled reminder succeeds")
    void test_cancel_alreadyCancelled_succeeds() {
        reminder.setIsDelivered(false);

        reminder.cancel();

        assertFalse(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("reschedule() - Reschedules to valid future time")
    void test_reschedule_validFutureTime_succeeds() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);

        reminder.reschedule(newTime);

        assertEquals(newTime, reminder.getScheduledTime());
    }

    @Test
    @DisplayName("reschedule() - Sets isDelivered to false")
    void test_reschedule_setsIsDeliveredFalse() {
        reminder.setIsDelivered(true);
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);

        reminder.reschedule(newTime);

        assertFalse(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("reschedule() - Past time throws IllegalArgumentException")
    void test_reschedule_pastTime_throwsException() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reminder.reschedule(pastTime)
        );
        assertEquals("New time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("reschedule() - Null time throws IllegalArgumentException")
    void test_reschedule_nullTime_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reminder.reschedule(null)
        );
        assertEquals("New time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("reschedule() - Updates scheduled time correctly")
    void test_reschedule_updatesScheduledTime() {
        LocalDateTime originalTime = reminder.getScheduledTime();
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);

        reminder.reschedule(newTime);

        assertNotEquals(originalTime, reminder.getScheduledTime());
        assertEquals(newTime, reminder.getScheduledTime());
    }

    @Test
    @DisplayName("deliver() - Marks scheduled reminder as delivered")
    void test_deliver_scheduledReminder_marksDelivered() {
        assertFalse(reminder.getIsDelivered());

        reminder.deliver();

        assertTrue(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("deliver() - Already delivered throws IllegalStateException")
    void test_deliver_alreadyDelivered_throwsException() {
        reminder.setIsDelivered(true);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            reminder::deliver
        );
        assertEquals("Reminder already delivered", exception.getMessage());
    }

    @Test
    @DisplayName("deliver() - Sets isDelivered to true")
    void test_deliver_setsIsDeliveredTrue() {
        reminder.deliver();

        assertTrue(reminder.getIsDelivered());
    }

    @Test
    @DisplayName("Reminder - Properties are mutable")
    void test_reminder_propertiesAreMutable() {
        reminder.setMessage("Updated message");
        reminder.setChannel(NotificationChannel.EMAIL);
        reminder.setEntityType("TASK");

        assertEquals("Updated message", reminder.getMessage());
        assertEquals(NotificationChannel.EMAIL, reminder.getChannel());
        assertEquals("TASK", reminder.getEntityType());
    }

    @Test
    @DisplayName("Reminder - Can be associated with Note")
    void test_reminder_associatedWithNote() {
        Note note = new Note();
        note.setNoteId("note-1");

        reminder.setNote(note);

        assertEquals(note, reminder.getNote());
    }

    @Test
    @DisplayName("Reminder - Can be associated with TodoItem")
    void test_reminder_associatedWithTodoItem() {
        TodoItem task = new TodoItem();
        task.setTaskId("task-1");

        reminder.setTodoItem(task);

        assertEquals(task, reminder.getTodoItem());
    }

    @Test
    @DisplayName("Reminder - Default isDelivered is false")
    void test_reminder_defaultIsDeliveredFalse() {
        Reminder newReminder = new Reminder();

        assertFalse(newReminder.getIsDelivered());
    }

    @Test
    @DisplayName("Reminder - Supports all notification channels")
    void test_reminder_supportsAllChannels() {
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
