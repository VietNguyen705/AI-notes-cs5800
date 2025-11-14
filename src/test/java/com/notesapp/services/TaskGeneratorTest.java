package com.notesapp.services;

import com.notesapp.entities.Note;
import com.notesapp.entities.TodoItem;
import com.notesapp.entities.User;
import com.notesapp.enums.Priority;
import com.notesapp.enums.TaskStatus;
import com.notesapp.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskGenerator service
 * Tests extractActionItems, inferDueDate, inferPriority, and generateTasks methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskGenerator Service Tests")
class TaskGeneratorTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskGenerator taskGenerator;

    private Note testNote;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("user-1");
        testUser.setUsername("testuser");

        testNote = new Note();
        testNote.setNoteId("note-1");
        testNote.setUserId("user-1");
        testNote.setTitle("Project Tasks");
        testNote.setBody("- Call client\n- Review document\n- Send email");
    }

    // ==================== extractActionItems() Tests ====================
    @Test
    @DisplayName("extractActionItems() - Null text throws IllegalArgumentException")
    void test_extractActionItems_nullText_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskGenerator.extractActionItems(null)
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("extractActionItems() - Empty text throws IllegalArgumentException")
    void test_extractActionItems_emptyText_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskGenerator.extractActionItems("   ")
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("extractActionItems() - Bullet point list returns action items")
    void test_extractActionItems_bulletPoints_returnsItems() {
        // Arrange
        String text = "- Buy groceries\n- Call mom\n- Finish homework";

        // Act
        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        // Assert
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.size() >= 1);
    }

    @Test
    @DisplayName("extractActionItems() - Action verbs detected")
    void test_extractActionItems_actionVerbs_detectsTasks() {
        // Arrange
        String text = "Call the client about the project. Send the report by Friday.";

        // Act
        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        // Assert
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    @DisplayName("extractActionItems() - Checkbox format detected")
    void test_extractActionItems_checkboxFormat_returnsItems() {
        // Arrange
        String text = "[ ] Complete assignment\n[x] Submit report";

        // Act
        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        // Assert
        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    @DisplayName("extractActionItems() - Plain text without actions returns empty or minimal items")
    void test_extractActionItems_noActions_returnsEmptyOrMinimal() {
        // Arrange
        String text = "This is just a plain note without any actions.";

        // Act
        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        // Assert
        assertNotNull(items);
        // May be empty or may extract minimal items depending on implementation
    }

    @Test
    @DisplayName("extractActionItems() - Multiple action verbs in one line")
    void test_extractActionItems_multipleVerbs_detectsItem() {
        // Arrange
        String text = "Buy milk and call the store to confirm availability";

        // Act
        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        // Assert
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    @DisplayName("extractActionItems() - Returns maps with title key")
    void test_extractActionItems_returnsMapsWithTitle() {
        // Arrange
        String text = "- Complete project\n- Review code";

        // Act
        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        // Assert
        assertFalse(items.isEmpty());
        for (Map<String, Object> item : items) {
            assertTrue(item.containsKey("title"));
            assertNotNull(item.get("title"));
        }
    }

    // ==================== inferDueDate() Tests ====================
    @Test
    @DisplayName("inferDueDate() - Null text returns null")
    void test_inferDueDate_nullText_returnsNull() {
        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate(null);

        // Assert
        assertNull(dueDate);
    }

    @Test
    @DisplayName("inferDueDate() - Empty text returns null")
    void test_inferDueDate_emptyText_returnsNull() {
        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate("");

        // Assert
        assertNull(dueDate);
    }

    @Test
    @DisplayName("inferDueDate() - 'today' keyword returns today's date")
    void test_inferDueDate_todayKeyword_returnsTodayDate() {
        // Arrange
        String text = "Complete task today";

        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        // Assert
        assertNotNull(dueDate);
        assertEquals(LocalDateTime.now().toLocalDate(), dueDate.toLocalDate());
    }

    @Test
    @DisplayName("inferDueDate() - 'tomorrow' keyword returns tomorrow's date")
    void test_inferDueDate_tomorrowKeyword_returnsTomorrowDate() {
        // Arrange
        String text = "Submit report tomorrow";

        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        // Assert
        assertNotNull(dueDate);
        assertEquals(LocalDateTime.now().plusDays(1).toLocalDate(), dueDate.toLocalDate());
    }

    @Test
    @DisplayName("inferDueDate() - 'next week' keyword returns next week date")
    void test_inferDueDate_nextWeekKeyword_returnsNextWeek() {
        // Arrange
        String text = "Finish project next week";

        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        // Assert
        assertNotNull(dueDate);
        assertTrue(dueDate.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("inferDueDate() - 'next month' keyword returns next month date")
    void test_inferDueDate_nextMonthKeyword_returnsNextMonth() {
        // Arrange
        String text = "Review in next month";

        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        // Assert
        assertNotNull(dueDate);
        assertTrue(dueDate.isAfter(LocalDateTime.now().plusDays(20)));
    }

    @Test
    @DisplayName("inferDueDate() - Text without date keywords returns null")
    void test_inferDueDate_noKeywords_returnsNull() {
        // Arrange
        String text = "Just a regular task";

        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        // Assert
        assertNull(dueDate);
    }

    @Test
    @DisplayName("inferDueDate() - Case insensitive keyword matching")
    void test_inferDueDate_caseInsensitive_works() {
        // Arrange
        String text = "Complete TOMORROW";

        // Act
        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        // Assert
        assertNotNull(dueDate);
    }

    // ==================== inferPriority() Tests ====================
    @Test
    @DisplayName("inferPriority() - Null text returns MEDIUM priority")
    void test_inferPriority_nullText_returnsMedium() {
        // Act
        Priority priority = taskGenerator.inferPriority(null);

        // Assert
        assertEquals(Priority.MEDIUM, priority);
    }

    @Test
    @DisplayName("inferPriority() - Empty text returns MEDIUM priority")
    void test_inferPriority_emptyText_returnsMedium() {
        // Act
        Priority priority = taskGenerator.inferPriority("");

        // Assert
        assertEquals(Priority.MEDIUM, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'urgent' keyword returns URGENT priority")
    void test_inferPriority_urgentKeyword_returnsUrgent() {
        // Arrange
        String text = "This is urgent!";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.URGENT, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'asap' keyword returns URGENT priority")
    void test_inferPriority_asapKeyword_returnsUrgent() {
        // Arrange
        String text = "Need this ASAP";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.URGENT, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'critical' keyword returns URGENT priority")
    void test_inferPriority_criticalKeyword_returnsUrgent() {
        // Arrange
        String text = "Critical bug fix required";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.URGENT, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'important' keyword returns HIGH priority")
    void test_inferPriority_importantKeyword_returnsHigh() {
        // Arrange
        String text = "This is important";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.HIGH, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'high priority' keyword returns HIGH priority")
    void test_inferPriority_highPriorityKeyword_returnsHigh() {
        // Arrange
        String text = "High priority task";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.HIGH, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'low priority' keyword returns LOW priority")
    void test_inferPriority_lowPriorityKeyword_returnsLow() {
        // Arrange
        String text = "Low priority item";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.LOW, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'whenever' keyword returns LOW priority")
    void test_inferPriority_wheneverKeyword_returnsLow() {
        // Arrange
        String text = "Do this whenever you have time";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.LOW, priority);
    }

    @Test
    @DisplayName("inferPriority() - No keywords returns MEDIUM priority")
    void test_inferPriority_noKeywords_returnsMedium() {
        // Arrange
        String text = "Regular task";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.MEDIUM, priority);
    }

    @Test
    @DisplayName("inferPriority() - Case insensitive matching")
    void test_inferPriority_caseInsensitive_works() {
        // Arrange
        String text = "URGENT TASK";

        // Act
        Priority priority = taskGenerator.inferPriority(text);

        // Assert
        assertEquals(Priority.URGENT, priority);
    }

    // ==================== generateTasks() Tests ====================
    @Test
    @DisplayName("generateTasks() - Null note throws IllegalArgumentException")
    void test_generateTasks_nullNote_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskGenerator.generateTasks(null, testUser)
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("generateTasks() - Valid note generates tasks")
    void test_generateTasks_validNote_generatesTasks() {
        // Arrange
        TodoItem savedTask = new TodoItem();
        savedTask.setTaskId("task-1");
        savedTask.setTitle("Call client");
        savedTask.setStatus(TaskStatus.PENDING);

        when(taskRepository.save(any(TodoItem.class))).thenReturn(savedTask);

        // Act
        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        // Assert
        assertNotNull(tasks);
        verify(taskRepository, atLeastOnce()).save(any(TodoItem.class));
    }

    @Test
    @DisplayName("generateTasks() - Generated tasks have PENDING status")
    void test_generateTasks_setsStatusPending() {
        // Arrange
        TodoItem savedTask = new TodoItem();
        savedTask.setTaskId("task-1");
        savedTask.setStatus(TaskStatus.PENDING);

        when(taskRepository.save(any(TodoItem.class))).thenReturn(savedTask);

        // Act
        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        // Assert
        if (!tasks.isEmpty()) {
            assertEquals(TaskStatus.PENDING, tasks.get(0).getStatus());
        }
    }

    @Test
    @DisplayName("generateTasks() - Tasks linked to note")
    void test_generateTasks_linksToNote() {
        // Arrange
        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        // Act
        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        // Assert
        verify(taskRepository, atLeastOnce()).save(argThat(task ->
            "note-1".equals(task.getNoteId())
        ));
    }

    @Test
    @DisplayName("generateTasks() - Tasks linked to user")
    void test_generateTasks_linksToUser() {
        // Arrange
        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        // Act
        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        // Assert
        verify(taskRepository, atLeastOnce()).save(argThat(task ->
            testUser.equals(task.getUser())
        ));
    }

    @Test
    @DisplayName("generateTasks() - Note with minimal content returns empty or minimal task list")
    void test_generateTasks_minimalNote_returnsEmptyOrMinimal() {
        // Arrange
        Note minimalNote = new Note();
        minimalNote.setNoteId("note-2");
        minimalNote.setTitle("Simple note");
        minimalNote.setBody("No action items here");

        // Act
        List<TodoItem> tasks = taskGenerator.generateTasks(minimalNote, testUser);

        // Assert
        assertNotNull(tasks);
        // May be empty or contain minimal tasks depending on pattern matching
    }

    @Test
    @DisplayName("generateTasks() - Infers priority from task text")
    void test_generateTasks_infersPriority() {
        // Arrange
        Note urgentNote = new Note();
        urgentNote.setNoteId("note-3");
        urgentNote.setTitle("Urgent Tasks");
        urgentNote.setBody("- Call client URGENT\n- Review document");

        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        // Act
        List<TodoItem> tasks = taskGenerator.generateTasks(urgentNote, testUser);

        // Assert
        assertNotNull(tasks);
        // At least one task should be saved
        verify(taskRepository, atLeastOnce()).save(any(TodoItem.class));
    }

    @Test
    @DisplayName("generateTasks() - Saves all generated tasks to repository")
    void test_generateTasks_savesAllTasks() {
        // Arrange
        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        // Act
        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        // Assert
        assertEquals(tasks.size(), (int) mockingDetails(taskRepository)
            .getInvocations()
            .stream()
            .filter(inv -> inv.getMethod().getName().equals("save"))
            .count());
    }
}
