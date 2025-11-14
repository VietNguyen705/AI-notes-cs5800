package com.notesapp.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Tag entity
 * Tests attachToNote and detachFromNote methods
 */
@DisplayName("Tag Entity Tests")
class TagTest {

    private Tag tag;
    private Note testNote;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setTagId("tag-1");
        tag.setName("TestTag");
        tag.setColor("#FF0000");

        testNote = new Note();
        testNote.setNoteId("note-1");
        testNote.setTitle("Test Note");
    }

    // ==================== attachToNote() Tests ====================
    @Test
    @DisplayName("attachToNote() - Null note is handled gracefully")
    void test_attachToNote_nullNote_noError() {
        // Act
        tag.attachToNote(null);

        // Assert
        assertTrue(tag.getNotes().isEmpty());
    }

    // ==================== detachFromNote() Tests ====================
    @Test
    @DisplayName("detachFromNote() - Null note is handled gracefully")
    void test_detachFromNote_nullNote_noError() {
        // Act
        tag.detachFromNote(null);

        // Assert
        assertTrue(tag.getNotes().isEmpty());
    }

    @Test
    @DisplayName("detachFromNote() - Detaching non-attached note succeeds")
    void test_detachFromNote_notAttachedNote_succeeds() {
        // Act
        tag.detachFromNote(testNote);

        // Assert
        assertFalse(tag.getNotes().contains(testNote));
    }

    // ==================== Entity Properties Tests ====================
    @Test
    @DisplayName("Tag - Properties are mutable")
    void test_tag_propertiesAreMutable() {
        // Act
        tag.setName("NewName");
        tag.setColor("#00FF00");

        // Assert
        assertEquals("NewName", tag.getName());
        assertEquals("#00FF00", tag.getColor());
    }

    @Test
    @DisplayName("Tag - Notes collection is initialized")
    void test_tag_notesInitialized() {
        // Assert
        assertNotNull(tag.getNotes());
        assertTrue(tag.getNotes().isEmpty());
    }

    @Test
    @DisplayName("Tag - Can be created with properties")
    void test_tag_propertiesSet() {
        // Assert
        assertEquals("tag-1", tag.getTagId());
        assertEquals("TestTag", tag.getName());
        assertEquals("#FF0000", tag.getColor());
    }
}
