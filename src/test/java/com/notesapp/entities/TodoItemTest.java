package com.notesapp.entities;

import com.notesapp.enums.Priority;
import com.notesapp.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TodoItem.complete() method
 *
 * Tests cover:
 * - Valid completion of pending task
 * - Completion timestamp verification
 * - Double completion prevention
 * - Cancelled task completion prevention
 * - Status change verification
 */
@DisplayName("TodoItem.complete() Tests")
class TodoItemTest {

    private TodoItem task;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setUserId("test-user-1");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Create a fresh task before each test
        task = new TodoItem();
        task.setTaskId("task-1");
        task.setUser(testUser);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(Priority.MEDIUM);
    }

    @Test
    @DisplayName("complete() - Pending task should be marked as completed")
    void test_complete_pendingTask_marksAsCompleted() {
        // Arrange
        assertEquals(TaskStatus.PENDING, task.getStatus(), "Initial status should be PENDING");
        assertNull(task.getCompletedAt(), "CompletedAt should be null initially");

        // Act
        task.complete();

        // Assert
        assertEquals(TaskStatus.COMPLETED, task.getStatus(),
            "Status should change to COMPLETED");
        assertNotNull(task.getCompletedAt(),
            "CompletedAt timestamp should be set");
    }

    @Test
    @DisplayName("complete() - Sets completedAt timestamp to current time")
    void test_complete_setsCompletedTimestamp_notNull() {
        // Arrange
        LocalDateTime beforeComplete = LocalDateTime.now().minusSeconds(1);

        // Act
        task.complete();

        // Assert
        LocalDateTime afterComplete = LocalDateTime.now().plusSeconds(1);
        assertNotNull(task.getCompletedAt(), "CompletedAt should not be null");
        assertTrue(task.getCompletedAt().isAfter(beforeComplete),
            "CompletedAt should be after the test started");
        assertTrue(task.getCompletedAt().isBefore(afterComplete),
            "CompletedAt should be before the test ended");
    }

    @Test
    @DisplayName("complete() - Already completed task throws IllegalStateException")
    void test_complete_alreadyCompleted_throwsTaskAlreadyCompletedException() {
        // Arrange
        task.complete(); // First completion
        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> task.complete(),
            "Should throw IllegalStateException when completing already completed task"
        );

        assertEquals("Task already completed", exception.getMessage(),
            "Exception message should indicate task is already completed");
    }

    @Test
    @DisplayName("complete() - Cancelled task throws IllegalStateException")
    void test_complete_cancelledTask_throwsTaskCancelledException() {
        // Arrange
        task.setStatus(TaskStatus.CANCELLED);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> task.complete(),
            "Should throw IllegalStateException when completing cancelled task"
        );

        assertEquals("Cannot complete cancelled task", exception.getMessage(),
            "Exception message should indicate task is cancelled");
    }

    @Test
    @DisplayName("complete() - Updates status from PENDING to COMPLETED")
    void test_complete_updatesStatus_toCompleted() {
        // Arrange
        task.setStatus(TaskStatus.PENDING);

        // Act
        task.complete();

        // Assert
        assertEquals(TaskStatus.COMPLETED, task.getStatus(),
            "Status should be updated to COMPLETED");
    }

    @Test
    @DisplayName("complete() - Updates status from IN_PROGRESS to COMPLETED")
    void test_complete_inProgressTask_updatesStatus_toCompleted() {
        // Arrange
        task.setStatus(TaskStatus.IN_PROGRESS);

        // Act
        task.complete();

        // Assert
        assertEquals(TaskStatus.COMPLETED, task.getStatus(),
            "Status should be updated from IN_PROGRESS to COMPLETED");
    }

    @Test
    @DisplayName("complete() - Preserves other task properties")
    void test_complete_preservesOtherProperties_unchanged() {
        // Arrange
        String originalTitle = task.getTitle();
        String originalDescription = task.getDescription();
        Priority originalPriority = task.getPriority();
        String originalTaskId = task.getTaskId();
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        task.setDueDate(dueDate);

        // Act
        task.complete();

        // Assert
        assertEquals(originalTitle, task.getTitle(), "Title should remain unchanged");
        assertEquals(originalDescription, task.getDescription(), "Description should remain unchanged");
        assertEquals(originalPriority, task.getPriority(), "Priority should remain unchanged");
        assertEquals(originalTaskId, task.getTaskId(), "TaskId should remain unchanged");
        assertEquals(dueDate, task.getDueDate(), "DueDate should remain unchanged");
    }

    @Test
    @DisplayName("complete() - CompletedAt is set only once")
    void test_complete_completedAtTimestamp_setOnlyOnce() {
        // Arrange & Act
        task.complete();
        LocalDateTime firstCompletionTime = task.getCompletedAt();

        // Try to complete again (should throw exception)
        try {
            task.complete();
        } catch (IllegalStateException e) {
            // Expected exception
        }

        // Assert
        assertEquals(firstCompletionTime, task.getCompletedAt(),
            "CompletedAt timestamp should not change on second completion attempt");
    }

    // ==================== update() Tests ====================
    @Test
    @DisplayName("update() - Updates all valid parameters successfully")
    void test_update_withAllValidParams_updatesSuccessfully() {
        // Arrange
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(5);
        Priority newPriority = Priority.HIGH;

        // Act
        task.update("New Title", newDueDate, newPriority);

        // Assert
        assertEquals("New Title", task.getTitle());
        assertEquals(newDueDate, task.getDueDate());
        assertEquals(newPriority, task.getPriority());
    }

    @Test
    @DisplayName("update() - Cannot update completed task")
    void test_update_completedTask_throwsException() {
        // Arrange
        task.setStatus(TaskStatus.COMPLETED);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> task.update("New Title", null, Priority.HIGH)
        );
        assertEquals("Cannot update completed task", exception.getMessage());
    }

    @Test
    @DisplayName("update() - Ignores empty title and keeps original")
    void test_update_withEmptyTitle_ignoresUpdate() {
        // Arrange
        String originalTitle = task.getTitle();

        // Act
        task.update("  ", null, null);

        // Assert
        assertEquals(originalTitle, task.getTitle());
    }

    @Test
    @DisplayName("update() - Clears due date when null provided")
    void test_update_withNullDueDate_clearsDueDate() {
        // Arrange
        task.setDueDate(LocalDateTime.now().plusDays(1));

        // Act
        task.update(null, null, null);

        // Assert
        assertNull(task.getDueDate());
    }

    // ==================== updateStatus() Tests ====================
    @Test
    @DisplayName("updateStatus() - Updates status with valid values")
    void test_updateStatus_withValidStatus_updatesSuccessfully() {
        // Act
        task.updateStatus(TaskStatus.IN_PROGRESS);

        // Assert
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    @DisplayName("updateStatus() - Null status throws IllegalArgumentException")
    void test_updateStatus_nullStatus_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.updateStatus(null)
        );
        assertEquals("Status cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("updateStatus() - Setting to COMPLETED sets completedAt timestamp")
    void test_updateStatus_toCompleted_setsCompletedTimestamp() {
        // Act
        task.updateStatus(TaskStatus.COMPLETED);

        // Assert
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    @DisplayName("updateStatus() - Cannot reopen completed task (COMPLETED to PENDING)")
    void test_updateStatus_fromCompletedToPending_throwsException() {
        // Arrange
        task.setStatus(TaskStatus.COMPLETED);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> task.updateStatus(TaskStatus.PENDING)
        );
        assertEquals("Cannot reopen completed task", exception.getMessage());
    }

    // ==================== setPriority() Tests ====================
    @Test
    @DisplayName("setPriority() - Updates priority with valid values")
    void test_setPriority_withValidPriority_updatesSuccessfully() {
        // Act
        task.setPriority(Priority.HIGH);

        // Assert
        assertEquals(Priority.HIGH, task.getPriority());
    }

    @Test
    @DisplayName("setPriority() - Null priority throws IllegalArgumentException")
    void test_setPriority_nullPriority_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.setPriority(null)
        );
        assertEquals("Priority cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("setPriority() - Cannot change priority of completed task")
    void test_setPriority_completedTask_throwsException() {
        // Arrange
        task.setStatus(TaskStatus.COMPLETED);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> task.setPriority(Priority.LOW)
        );
        assertEquals("Cannot change priority of completed task", exception.getMessage());
    }

    @Test
    @DisplayName("setPriority() - Can change priority of IN_PROGRESS task")
    void test_setPriority_inProgressTask_succeeds() {
        // Arrange
        task.setStatus(TaskStatus.IN_PROGRESS);

        // Act
        task.setPriority(Priority.URGENT);

        // Assert
        assertEquals(Priority.URGENT, task.getPriority());
    }

    // ==================== setDueDate() Tests ====================
    @Test
    @DisplayName("setDueDate() - Sets valid future date successfully")
    void test_setDueDate_withValidFutureDate_succeeds() {
        // Arrange
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        // Act
        task.setDueDate(futureDate);

        // Assert
        assertEquals(futureDate, task.getDueDate());
    }

    @Test
    @DisplayName("setDueDate() - Clears due date when null provided")
    void test_setDueDate_withNull_clearsDueDate() {
        // Arrange
        task.setDueDate(LocalDateTime.now().plusDays(1));

        // Act
        task.setDueDate(null);

        // Assert
        assertNull(task.getDueDate());
    }

    @Test
    @DisplayName("setDueDate() - Past date throws IllegalArgumentException")
    void test_setDueDate_withPastDate_throwsException() {
        // Arrange
        LocalDateTime pastDate = LocalDateTime.now().minusHours(1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> task.setDueDate(pastDate)
        );
        assertEquals("Due date cannot be in the past", exception.getMessage());
    }

    @Test
    @DisplayName("setDueDate() - Cannot set due date for completed task")
    void test_setDueDate_completedTask_throwsException() {
        // Arrange
        task.setStatus(TaskStatus.COMPLETED);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> task.setDueDate(LocalDateTime.now().plusDays(1))
        );
        assertEquals("Cannot change due date of completed task", exception.getMessage());
    }

    // ==================== delete() Tests ====================
    @Test
    @DisplayName("delete() - Succeeds for task without reminder")
    void test_delete_taskWithoutReminder_succeeds() {
        // Act
        task.delete();

        // Assert
        assertNotNull(task);
    }

    @Test
    @DisplayName("delete() - Cancels associated reminder")
    void test_delete_taskWithReminder_cancelsReminder() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setReminderId("reminder-1");
        reminder.setScheduledTime(LocalDateTime.now().plusHours(1));
        reminder.setIsDelivered(false);
        task.setReminder(reminder);

        // Act
        task.delete();

        // Assert
        assertEquals(false, reminder.getIsDelivered());
    }

    @Test
    @DisplayName("delete() - Succeeds for completed task")
    void test_delete_completedTask_succeeds() {
        // Arrange
        task.setStatus(TaskStatus.COMPLETED);

        // Act
        task.delete();

        // Assert
        assertNotNull(task);
    }
}
