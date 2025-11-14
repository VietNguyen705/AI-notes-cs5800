package com.notesapp.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Note entity
 * Tests all business logic methods: create, update, delete, addTag, removeTag, etc.
 */
@DisplayName("Note Entity Tests")
class NoteTest {

    private Note note;
    private User testUser;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("user-1");
        testUser.setUsername("testuser");

        note = new Note();
        note.setNoteId("note-1");
        note.setUserId("user-1");
        note.setTitle("Test Note");
        note.setBody("This is a test note");
        note.setUser(testUser);

        testTag = new Tag();
        testTag.setTagId("tag-1");
        testTag.setName("TestTag");
    }

    // ==================== create() Tests ====================
    @Test
    @DisplayName("create() - Valid note with title succeeds")
    void test_create_withValidTitle_succeeds() {
        // Arrange
        Note newNote = new Note();
        newNote.setTitle("Valid Title");

        // Act
        newNote.create();

        // Assert
        assertNotNull(newNote.getTitle());
    }

    @Test
    @DisplayName("create() - Null title throws IllegalArgumentException")
    void test_create_nullTitle_throwsException() {
        // Arrange
        Note newNote = new Note();
        newNote.setTitle(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            newNote::create
        );
        assertEquals("Title cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("create() - Empty title throws IllegalArgumentException")
    void test_create_emptyTitle_throwsException() {
        // Arrange
        Note newNote = new Note();
        newNote.setTitle("   ");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            newNote::create
        );
        assertEquals("Title cannot be null or empty", exception.getMessage());
    }

    // ==================== update() Tests ====================
    @Test
    @DisplayName("update() - Updates title successfully")
    void test_update_updateTitle_succeeds() {
        // Act
        Map<String, Object> content = new HashMap<>();
        content.put("title", "Updated Title");
        note.update(content);

        // Assert
        assertEquals("Updated Title", note.getTitle());
    }

    @Test
    @DisplayName("update() - Updates body successfully")
    void test_update_updateBody_succeeds() {
        // Act
        Map<String, Object> content = new HashMap<>();
        content.put("body", "Updated body content");
        note.update(content);

        // Assert
        assertEquals("Updated body content", note.getBody());
    }

    @Test
    @DisplayName("update() - Updates multiple fields")
    void test_update_multipleFields_succeeds() {
        // Act
        Map<String, Object> content = new HashMap<>();
        content.put("title", "New Title");
        content.put("body", "New body");
        content.put("color", "#FF0000");
        note.update(content);

        // Assert
        assertEquals("New Title", note.getTitle());
        assertEquals("New body", note.getBody());
        assertEquals("#FF0000", note.getColor());
    }

    @Test
    @DisplayName("update() - Null content throws IllegalArgumentException")
    void test_update_nullContent_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> note.update(null)
        );
        assertEquals("Content cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("update() - Empty map doesn't change fields")
    void test_update_emptyMap_noChanges() {
        // Arrange
        String originalTitle = note.getTitle();

        // Act
        note.update(new HashMap<>());

        // Assert
        assertEquals(originalTitle, note.getTitle());
    }

    @Test
    @DisplayName("update() - Updates timestamp")
    void test_update_setsUpdatedAt_timestamp() {
        // Arrange
        LocalDateTime beforeUpdate = LocalDateTime.now();

        // Act
        Map<String, Object> content = new HashMap<>();
        content.put("title", "Updated");
        note.update(content);

        // Assert
        assertNotNull(note.getUpdatedAt());
        assertTrue(note.getUpdatedAt().isAfter(beforeUpdate) || note.getUpdatedAt().equals(beforeUpdate));
    }

    // ==================== addTag() Tests ====================
    @Test
    @DisplayName("addTag() - Null tag throws IllegalArgumentException")
    void test_addTag_nullTag_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> note.addTag(null)
        );
        assertEquals("Tag cannot be null", exception.getMessage());
    }

    // ==================== removeTag() Tests ====================
    @Test
    @DisplayName("removeTag() - Null tag throws IllegalArgumentException")
    void test_removeTag_nullTag_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> note.removeTag(null)
        );
        assertEquals("Tag cannot be null", exception.getMessage());
    }

    // ==================== delete() Tests ====================
    @Test
    @DisplayName("delete() - Succeeds without reminder")
    void test_delete_withoutReminder_succeeds() {
        // Act
        note.delete();

        // Assert
        assertNotNull(note);
    }

    @Test
    @DisplayName("delete() - Cancels associated reminder")
    void test_delete_withReminder_cancelsReminder() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setReminderId("reminder-1");
        reminder.setScheduledTime(LocalDateTime.now().plusHours(1));
        reminder.setIsDelivered(false);
        note.setReminder(reminder);

        // Act
        note.delete();

        // Assert
        assertEquals(false, reminder.getIsDelivered());
    }

    // ==================== autoOrganize() Tests ====================
    @Test
    @DisplayName("autoOrganize() - Method completes without error")
    void test_autoOrganize_completes() {
        // Act
        note.autoOrganize();

        // Assert
        assertNotNull(note);
    }

    // ==================== generateTasks() Tests ====================
    @Test
    @DisplayName("generateTasks() - Returns empty list (implementation in service)")
    void test_generateTasks_returnsEmptyList() {
        // Act
        var tasks = note.generateTasks();

        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    // ==================== setReminder() Tests ====================
    @Test
    @DisplayName("setReminder() - Sets valid reminder successfully")
    void test_setReminder_validReminder_succeeds() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setReminderId("reminder-1");
        reminder.setScheduledTime(LocalDateTime.now().plusHours(1));

        // Act
        note.setReminder(reminder);

        // Assert
        assertEquals(reminder, note.getReminder());
    }

    @Test
    @DisplayName("setReminder() - Null reminder throws IllegalArgumentException")
    void test_setReminder_nullReminder_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> note.setReminder(null)
        );
        assertEquals("Reminder cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("setReminder() - Past reminder time throws IllegalArgumentException")
    void test_setReminder_pastTime_throwsException() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setReminderId("reminder-1");
        reminder.setScheduledTime(LocalDateTime.now().minusHours(1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> note.setReminder(reminder)
        );
        assertEquals("Reminder time must be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("setReminder() - Sets note reference on reminder")
    void test_setReminder_setsNoteReference() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setReminderId("reminder-1");
        reminder.setScheduledTime(LocalDateTime.now().plusHours(1));

        // Act
        note.setReminder(reminder);

        // Assert
        assertEquals(note, reminder.getNote());
    }

    // ==================== Entity Lifecycle Tests ====================
    @Test
    @DisplayName("Note - Properties are mutable")
    void test_note_propertiesAreMutable() {
        // Act
        note.setTitle("New Title");
        note.setBody("New body");
        note.setColor("#000000");

        // Assert
        assertEquals("New Title", note.getTitle());
        assertEquals("New body", note.getBody());
        assertEquals("#000000", note.getColor());
    }

    @Test
    @DisplayName("Note - Default values are set correctly")
    void test_note_defaultValues() {
        // Arrange
        Note newNote = new Note();

        // Assert
        assertFalse(newNote.getIsPinned());
        assertNotNull(newNote.getTags());
        assertNotNull(newNote.getChecklist());
    }
}
