package com.notesapp.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChecklistItem entity
 * Tests toggle() method for checking/unchecking items
 */
@DisplayName("ChecklistItem Entity Tests")
class ChecklistItemTest {

    private ChecklistItem item;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testNote = new Note();
        testNote.setNoteId("note-1");
        testNote.setTitle("Test Note");

        item = new ChecklistItem();
        item.setItemId("item-1");
        item.setText("Test checklist item");
        item.setIsChecked(false);
        item.setNote(testNote);
    }

    @Test
    @DisplayName("toggle() - Unchecked item becomes checked")
    void test_toggle_uncheckedItem_becomesChecked() {
        // Arrange
        assertFalse(item.getIsChecked());

        // Act
        item.toggle();

        // Assert
        assertTrue(item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Checked item becomes unchecked")
    void test_toggle_checkedItem_becomesUnchecked() {
        // Arrange
        item.setIsChecked(true);
        assertTrue(item.getIsChecked());

        // Act
        item.toggle();

        // Assert
        assertFalse(item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Multiple toggles result in correct state")
    void test_toggle_multipleToggles_correctFinalState() {
        // Arrange
        assertFalse(item.getIsChecked());

        // Act
        item.toggle(); // true
        item.toggle(); // false
        item.toggle(); // true

        // Assert
        assertTrue(item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Toggling twice returns to original state")
    void test_toggle_twice_returnsToOriginalState() {
        // Arrange
        boolean originalState = item.getIsChecked();

        // Act
        item.toggle();
        item.toggle();

        // Assert
        assertEquals(originalState, item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Preserves other properties")
    void test_toggle_preservesOtherProperties() {
        // Arrange
        String originalText = item.getText();
        Note originalNote = item.getNote();

        // Act
        item.toggle();

        // Assert
        assertEquals(originalText, item.getText());
        assertEquals(originalNote, item.getNote());
    }

    @Test
    @DisplayName("ChecklistItem - Can be created with checked state")
    void test_checklistItem_createdWithCheckedState() {
        // Arrange
        ChecklistItem checkedItem = new ChecklistItem();
        checkedItem.setItemId("item-2");
        checkedItem.setText("Done item");
        checkedItem.setIsChecked(true);

        // Act
        checkedItem.toggle();

        // Assert
        assertFalse(checkedItem.getIsChecked());
    }

    @Test
    @DisplayName("ChecklistItem - Properties are mutable")
    void test_checklistItem_propertiesAreMutable() {
        // Act
        item.setText("Updated text");
        item.setNote(testNote);

        // Assert
        assertEquals("Updated text", item.getText());
        assertEquals(testNote, item.getNote());
    }
}
