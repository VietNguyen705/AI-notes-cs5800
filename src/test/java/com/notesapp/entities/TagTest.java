package com.notesapp.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @DisplayName("attachToNote() - Null note is handled gracefully")
    void test_attachToNote_nullNote_noError() {
        tag.attachToNote(null);

        assertTrue(tag.getNotes().isEmpty());
    }

    @Test
    @DisplayName("detachFromNote() - Null note is handled gracefully")
    void test_detachFromNote_nullNote_noError() {
        tag.detachFromNote(null);

        assertTrue(tag.getNotes().isEmpty());
    }

    @Test
    @DisplayName("detachFromNote() - Detaching non-attached note succeeds")
    void test_detachFromNote_notAttachedNote_succeeds() {
        tag.detachFromNote(testNote);

        assertFalse(tag.getNotes().contains(testNote));
    }

    @Test
    @DisplayName("Tag - Properties are mutable")
    void test_tag_propertiesAreMutable() {
        tag.setName("NewName");
        tag.setColor("#00FF00");

        assertEquals("NewName", tag.getName());
        assertEquals("#00FF00", tag.getColor());
    }

    @Test
    @DisplayName("Tag - Notes collection is initialized")
    void test_tag_notesInitialized() {
        assertNotNull(tag.getNotes());
        assertTrue(tag.getNotes().isEmpty());
    }

    @Test
    @DisplayName("Tag - Can be created with properties")
    void test_tag_propertiesSet() {
        assertEquals("tag-1", tag.getTagId());
        assertEquals("TestTag", tag.getName());
        assertEquals("#FF0000", tag.getColor());
    }
}
