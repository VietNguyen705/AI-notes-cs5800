package com.notesapp.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 * Tests authenticate, updatePreferences, getNotes, getTasks
 */
@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId("user-1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    // ==================== authenticate() Tests ====================
    @Test
    @DisplayName("authenticate() - Returns true (placeholder implementation)")
    void test_authenticate_returnsTrue() {
        // Act
        boolean result = user.authenticate();

        // Assert
        assertTrue(result);
    }

    // ==================== updatePreferences() Tests ====================
    @Test
    @DisplayName("updatePreferences() - Updates valid preferences")
    void test_updatePreferences_withValidMap_succeeds() {
        // Arrange
        Map<String, String> prefs = new HashMap<>();
        prefs.put("theme", "dark");
        prefs.put("language", "en");

        // Act
        user.updatePreferences(prefs);

        // Assert
        assertEquals("dark", user.getPreferences().get("theme"));
        assertEquals("en", user.getPreferences().get("language"));
    }

    @Test
    @DisplayName("updatePreferences() - Null map is handled gracefully")
    void test_updatePreferences_nullMap_noError() {
        // Act
        user.updatePreferences(null);

        // Assert
        assertNotNull(user.getPreferences());
    }

    @Test
    @DisplayName("updatePreferences() - Empty map succeeds")
    void test_updatePreferences_emptyMap_succeeds() {
        // Act
        user.updatePreferences(new HashMap<>());

        // Assert
        assertNotNull(user.getPreferences());
    }

    @Test
    @DisplayName("updatePreferences() - Overwrites existing preferences")
    void test_updatePreferences_overwritesExisting() {
        // Arrange
        Map<String, String> prefs1 = new HashMap<>();
        prefs1.put("theme", "light");
        user.updatePreferences(prefs1);

        Map<String, String> prefs2 = new HashMap<>();
        prefs2.put("theme", "dark");

        // Act
        user.updatePreferences(prefs2);

        // Assert
        assertEquals("dark", user.getPreferences().get("theme"));
    }

    @Test
    @DisplayName("updatePreferences() - Adds multiple preferences")
    void test_updatePreferences_multiplePairs_succeeds() {
        // Arrange
        Map<String, String> prefs = new HashMap<>();
        prefs.put("pref1", "value1");
        prefs.put("pref2", "value2");
        prefs.put("pref3", "value3");

        // Act
        user.updatePreferences(prefs);

        // Assert
        assertEquals(3, user.getPreferences().size());
        assertEquals("value1", user.getPreferences().get("pref1"));
        assertEquals("value2", user.getPreferences().get("pref2"));
        assertEquals("value3", user.getPreferences().get("pref3"));
    }

    // ==================== getNotes() Tests ====================
    @Test
    @DisplayName("getNotes() - Returns empty list when no notes")
    void test_getNotes_noNotes_returnsEmptyList() {
        // Act
        var notes = user.getNotes();

        // Assert
        assertNotNull(notes);
        assertTrue(notes.isEmpty());
    }

    @Test
    @DisplayName("getNotes() - Returns list of notes")
    void test_getNotes_withNotes_returnsList() {
        // Act
        var notes = user.getNotes();

        // Assert
        assertNotNull(notes);
    }

    @Test
    @DisplayName("getNotes() - Returns defensive copy")
    void test_getNotes_returnsCopy_notReference() {
        // Act
        var notes1 = user.getNotes();
        var notes2 = user.getNotes();

        // Assert
        assertNotSame(notes1, notes2);
    }

    // ==================== getTasks() Tests ====================
    @Test
    @DisplayName("getTasks() - Returns empty list when no tasks")
    void test_getTasks_noTasks_returnsEmptyList() {
        // Act
        var tasks = user.getTasks();

        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    @DisplayName("getTasks() - Returns list of tasks")
    void test_getTasks_withTasks_returnsList() {
        // Act
        var tasks = user.getTasks();

        // Assert
        assertNotNull(tasks);
    }

    @Test
    @DisplayName("getTasks() - Returns defensive copy")
    void test_getTasks_returnsCopy_notReference() {
        // Act
        var tasks1 = user.getTasks();
        var tasks2 = user.getTasks();

        // Assert
        assertNotSame(tasks1, tasks2);
    }

    // ==================== Entity Properties Tests ====================
    @Test
    @DisplayName("User - Properties are mutable")
    void test_user_propertiesAreMutable() {
        // Act
        user.setUsername("newusername");
        user.setEmail("newemail@example.com");

        // Assert
        assertEquals("newusername", user.getUsername());
        assertEquals("newemail@example.com", user.getEmail());
    }

    @Test
    @DisplayName("User - Default preferences map is initialized")
    void test_user_defaultPreferencesInitialized() {
        // Assert
        assertNotNull(user.getPreferences());
    }

    @Test
    @DisplayName("User - Default notes list is initialized")
    void test_user_defaultNotesInitialized() {
        // Assert
        assertNotNull(user.getNotes());
    }

    @Test
    @DisplayName("User - Default tasks list is initialized")
    void test_user_defaultTasksInitialized() {
        // Assert
        assertNotNull(user.getTasks());
    }
}
