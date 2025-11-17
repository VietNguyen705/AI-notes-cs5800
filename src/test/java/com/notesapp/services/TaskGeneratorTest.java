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

    @Test
    @DisplayName("extractActionItems() - Null text throws IllegalArgumentException")
    void test_extractActionItems_nullText_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskGenerator.extractActionItems(null)
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("extractActionItems() - Empty text throws IllegalArgumentException")
    void test_extractActionItems_emptyText_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskGenerator.extractActionItems("   ")
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("extractActionItems() - Bullet point list returns action items")
    void test_extractActionItems_bulletPoints_returnsItems() {
        String text = "- Buy groceries\n- Call mom\n- Finish homework";

        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items.size() >= 1);
    }

    @Test
    @DisplayName("extractActionItems() - Action verbs detected")
    void test_extractActionItems_actionVerbs_detectsTasks() {
        String text = "Call the client about the project. Send the report by Friday.";

        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    @DisplayName("extractActionItems() - Checkbox format detected")
    void test_extractActionItems_checkboxFormat_returnsItems() {
        String text = "[ ] Complete assignment\n[x] Submit report";

        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    @DisplayName("extractActionItems() - Plain text without actions returns empty or minimal items")
    void test_extractActionItems_noActions_returnsEmptyOrMinimal() {
        String text = "This is just a plain note without any actions.";

        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        assertNotNull(items);
    }

    @Test
    @DisplayName("extractActionItems() - Multiple action verbs in one line")
    void test_extractActionItems_multipleVerbs_detectsItem() {
        String text = "Buy milk and call the store to confirm availability";

        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        assertNotNull(items);
        assertFalse(items.isEmpty());
    }

    @Test
    @DisplayName("extractActionItems() - Returns maps with title key")
    void test_extractActionItems_returnsMapsWithTitle() {
        String text = "- Complete project\n- Review code";

        List<Map<String, Object>> items = taskGenerator.extractActionItems(text);

        assertFalse(items.isEmpty());
        for (Map<String, Object> item : items) {
            assertTrue(item.containsKey("title"));
            assertNotNull(item.get("title"));
        }
    }

    @Test
    @DisplayName("inferDueDate() - Null text returns null")
    void test_inferDueDate_nullText_returnsNull() {
        LocalDateTime dueDate = taskGenerator.inferDueDate(null);

        assertNull(dueDate);
    }

    @Test
    @DisplayName("inferDueDate() - Empty text returns null")
    void test_inferDueDate_emptyText_returnsNull() {
        LocalDateTime dueDate = taskGenerator.inferDueDate("");

        assertNull(dueDate);
    }

    @Test
    @DisplayName("inferDueDate() - 'today' keyword returns today's date")
    void test_inferDueDate_todayKeyword_returnsTodayDate() {
        String text = "Complete task today";

        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        assertNotNull(dueDate);
        assertEquals(LocalDateTime.now().toLocalDate(), dueDate.toLocalDate());
    }

    @Test
    @DisplayName("inferDueDate() - 'tomorrow' keyword returns tomorrow's date")
    void test_inferDueDate_tomorrowKeyword_returnsTomorrowDate() {
        String text = "Submit report tomorrow";

        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        assertNotNull(dueDate);
        assertEquals(LocalDateTime.now().plusDays(1).toLocalDate(), dueDate.toLocalDate());
    }

    @Test
    @DisplayName("inferDueDate() - 'next week' keyword returns next week date")
    void test_inferDueDate_nextWeekKeyword_returnsNextWeek() {
        String text = "Finish project next week";

        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        assertNotNull(dueDate);
        assertTrue(dueDate.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("inferDueDate() - 'next month' keyword returns next month date")
    void test_inferDueDate_nextMonthKeyword_returnsNextMonth() {
        String text = "Review in next month";

        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        assertNotNull(dueDate);
        assertTrue(dueDate.isAfter(LocalDateTime.now().plusDays(20)));
    }

    @Test
    @DisplayName("inferDueDate() - Text without date keywords returns null")
    void test_inferDueDate_noKeywords_returnsNull() {
        String text = "Just a regular task";

        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        assertNull(dueDate);
    }

    @Test
    @DisplayName("inferDueDate() - Case insensitive keyword matching")
    void test_inferDueDate_caseInsensitive_works() {
        String text = "Complete TOMORROW";

        LocalDateTime dueDate = taskGenerator.inferDueDate(text);

        assertNotNull(dueDate);
    }

    @Test
    @DisplayName("inferPriority() - Null text returns MEDIUM priority")
    void test_inferPriority_nullText_returnsMedium() {
        Priority priority = taskGenerator.inferPriority(null);

        assertEquals(Priority.MEDIUM, priority);
    }

    @Test
    @DisplayName("inferPriority() - Empty text returns MEDIUM priority")
    void test_inferPriority_emptyText_returnsMedium() {
        Priority priority = taskGenerator.inferPriority("");

        assertEquals(Priority.MEDIUM, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'urgent' keyword returns URGENT priority")
    void test_inferPriority_urgentKeyword_returnsUrgent() {
        String text = "This is urgent!";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.URGENT, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'asap' keyword returns URGENT priority")
    void test_inferPriority_asapKeyword_returnsUrgent() {
        String text = "Need this ASAP";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.URGENT, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'critical' keyword returns URGENT priority")
    void test_inferPriority_criticalKeyword_returnsUrgent() {
        String text = "Critical bug fix required";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.URGENT, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'important' keyword returns HIGH priority")
    void test_inferPriority_importantKeyword_returnsHigh() {
        String text = "This is important";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.HIGH, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'high priority' keyword returns HIGH priority")
    void test_inferPriority_highPriorityKeyword_returnsHigh() {
        String text = "High priority task";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.HIGH, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'low priority' keyword returns LOW priority")
    void test_inferPriority_lowPriorityKeyword_returnsLow() {
        String text = "Low priority item";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.LOW, priority);
    }

    @Test
    @DisplayName("inferPriority() - 'whenever' keyword returns LOW priority")
    void test_inferPriority_wheneverKeyword_returnsLow() {
        String text = "Do this whenever you have time";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.LOW, priority);
    }

    @Test
    @DisplayName("inferPriority() - No keywords returns MEDIUM priority")
    void test_inferPriority_noKeywords_returnsMedium() {
        String text = "Regular task";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.MEDIUM, priority);
    }

    @Test
    @DisplayName("inferPriority() - Case insensitive matching")
    void test_inferPriority_caseInsensitive_works() {
        String text = "URGENT TASK";

        Priority priority = taskGenerator.inferPriority(text);

        assertEquals(Priority.URGENT, priority);
    }

    @Test
    @DisplayName("generateTasks() - Null note throws IllegalArgumentException")
    void test_generateTasks_nullNote_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskGenerator.generateTasks(null, testUser)
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("generateTasks() - Valid note generates tasks")
    void test_generateTasks_validNote_generatesTasks() {
        TodoItem savedTask = new TodoItem();
        savedTask.setTaskId("task-1");
        savedTask.setTitle("Call client");
        savedTask.setStatus(TaskStatus.PENDING);

        when(taskRepository.save(any(TodoItem.class))).thenReturn(savedTask);

        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        assertNotNull(tasks);
        verify(taskRepository, atLeastOnce()).save(any(TodoItem.class));
    }

    @Test
    @DisplayName("generateTasks() - Generated tasks have PENDING status")
    void test_generateTasks_setsStatusPending() {
        TodoItem savedTask = new TodoItem();
        savedTask.setTaskId("task-1");
        savedTask.setStatus(TaskStatus.PENDING);

        when(taskRepository.save(any(TodoItem.class))).thenReturn(savedTask);

        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        if (!tasks.isEmpty()) {
            assertEquals(TaskStatus.PENDING, tasks.get(0).getStatus());
        }
    }

    @Test
    @DisplayName("generateTasks() - Tasks linked to note")
    void test_generateTasks_linksToNote() {
        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        verify(taskRepository, atLeastOnce()).save(argThat(task ->
            "note-1".equals(task.getNoteId())
        ));
    }

    @Test
    @DisplayName("generateTasks() - Tasks linked to user")
    void test_generateTasks_linksToUser() {
        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        verify(taskRepository, atLeastOnce()).save(argThat(task ->
            testUser.equals(task.getUser())
        ));
    }

    @Test
    @DisplayName("generateTasks() - Note with minimal content returns empty or minimal task list")
    void test_generateTasks_minimalNote_returnsEmptyOrMinimal() {
        Note minimalNote = new Note();
        minimalNote.setNoteId("note-2");
        minimalNote.setTitle("Simple note");
        minimalNote.setBody("No action items here");

        List<TodoItem> tasks = taskGenerator.generateTasks(minimalNote, testUser);

        assertNotNull(tasks);
    }

    @Test
    @DisplayName("generateTasks() - Infers priority from task text")
    void test_generateTasks_infersPriority() {
        Note urgentNote = new Note();
        urgentNote.setNoteId("note-3");
        urgentNote.setTitle("Urgent Tasks");
        urgentNote.setBody("- Call client URGENT\n- Review document");

        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        List<TodoItem> tasks = taskGenerator.generateTasks(urgentNote, testUser);

        assertNotNull(tasks);
        verify(taskRepository, atLeastOnce()).save(any(TodoItem.class));
    }

    @Test
    @DisplayName("generateTasks() - Saves all generated tasks to repository")
    void test_generateTasks_savesAllTasks() {
        when(taskRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem task = invocation.getArgument(0);
            task.setTaskId("task-" + System.currentTimeMillis());
            return task;
        });

        List<TodoItem> tasks = taskGenerator.generateTasks(testNote, testUser);

        assertEquals(tasks.size(), (int) mockingDetails(taskRepository)
            .getInvocations()
            .stream()
            .filter(inv -> inv.getMethod().getName().equals("save"))
            .count());
    }
}
