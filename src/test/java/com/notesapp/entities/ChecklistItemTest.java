package com.notesapp.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

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
        assertFalse(item.getIsChecked());

        item.toggle();

        assertTrue(item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Checked item becomes unchecked")
    void test_toggle_checkedItem_becomesUnchecked() {
        item.setIsChecked(true);
        assertTrue(item.getIsChecked());

        item.toggle();

        assertFalse(item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Multiple toggles result in correct state")
    void test_toggle_multipleToggles_correctFinalState() {
        assertFalse(item.getIsChecked());

        item.toggle(); // true
        item.toggle(); // false
        item.toggle(); // true

        assertTrue(item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Toggling twice returns to original state")
    void test_toggle_twice_returnsToOriginalState() {
        boolean originalState = item.getIsChecked();

        item.toggle();
        item.toggle();

        assertEquals(originalState, item.getIsChecked());
    }

    @Test
    @DisplayName("toggle() - Preserves other properties")
    void test_toggle_preservesOtherProperties() {
        String originalText = item.getText();
        Note originalNote = item.getNote();

        item.toggle();

        assertEquals(originalText, item.getText());
        assertEquals(originalNote, item.getNote());
    }

    @Test
    @DisplayName("ChecklistItem - Can be created with checked state")
    void test_checklistItem_createdWithCheckedState() {
        ChecklistItem checkedItem = new ChecklistItem();
        checkedItem.setItemId("item-2");
        checkedItem.setText("Done item");
        checkedItem.setIsChecked(true);

        checkedItem.toggle();

        assertFalse(checkedItem.getIsChecked());
    }

    @Test
    @DisplayName("ChecklistItem - Properties are mutable")
    void test_checklistItem_propertiesAreMutable() {
        item.setText("Updated text");
        item.setNote(testNote);

        assertEquals("Updated text", item.getText());
        assertEquals(testNote, item.getNote());
    }
}
