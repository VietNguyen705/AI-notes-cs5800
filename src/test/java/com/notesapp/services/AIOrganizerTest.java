package com.notesapp.services;

import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.entities.Category;
import com.notesapp.repositories.TagRepository;
import com.notesapp.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AIOrganizer service
 * Tests analyzeContent, suggestTags, and categorize methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AIOrganizer Service Tests")
class AIOrganizerTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AIOrganizer aiOrganizer;

    private Note testNote;
    private Tag testTag;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testNote = new Note();
        testNote.setNoteId("note-1");
        testNote.setTitle("Meeting with client");
        testNote.setBody("Discuss project requirements and deadlines");

        testTag = new Tag();
        testTag.setTagId("tag-1");
        testTag.setName("Work");
        testTag.setColor("#FF0000");

        testCategory = new Category();
        testCategory.setCategoryId("cat-1");
        testCategory.setUserId("user-1");
        testCategory.setName("Work");
        testCategory.setDescription("work project meeting client");
    }

    // ==================== analyzeContent() Tests ====================
    @Test
    @DisplayName("analyzeContent() - Valid text returns analysis result")
    void test_analyzeContent_validText_returnsAnalysis() {
        // Arrange
        String text = "I need to finish the project report by tomorrow";

        // Act
        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("tags"));
        assertTrue(result.containsKey("category"));
        assertNotNull(result.get("tags"));
        assertNotNull(result.get("category"));
    }

    @Test
    @DisplayName("analyzeContent() - Null text throws IllegalArgumentException")
    void test_analyzeContent_nullText_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.analyzeContent(null)
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("analyzeContent() - Empty text throws IllegalArgumentException")
    void test_analyzeContent_emptyText_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.analyzeContent("   ")
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("analyzeContent() - Work-related text returns Work tag")
    void test_analyzeContent_workText_returnsWorkTag() {
        // Arrange
        String text = "Meeting with client about the project deadline";

        // Act
        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        // Assert
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("Work"));
    }

    @Test
    @DisplayName("analyzeContent() - Health-related text returns Health tag")
    void test_analyzeContent_healthText_returnsHealthTag() {
        // Arrange
        String text = "Doctor appointment for annual health checkup";

        // Act
        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        // Assert
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("Health"));
    }

    @Test
    @DisplayName("analyzeContent() - Shopping text returns Shopping tag")
    void test_analyzeContent_shoppingText_returnsShoppingTag() {
        // Arrange
        String text = "Buy groceries from the store";

        // Act
        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        // Assert
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("Shopping"));
    }

    @Test
    @DisplayName("analyzeContent() - Generic text returns General tag")
    void test_analyzeContent_genericText_returnsGeneralTag() {
        // Arrange
        String text = "Random note about something";

        // Act
        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        // Assert
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("General"));
    }

    @Test
    @DisplayName("analyzeContent() - Returns limited number of tags")
    void test_analyzeContent_limitsTagCount() {
        // Arrange
        String text = "Work meeting project client study learn homework";

        // Act
        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        // Assert
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.size() <= 3);
    }

    // ==================== suggestTags() Tests ====================
    @Test
    @DisplayName("suggestTags() - Null note throws IllegalArgumentException")
    void test_suggestTags_nullNote_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.suggestTags(null)
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("suggestTags() - Empty note returns empty list")
    void test_suggestTags_emptyNote_returnsEmptyList() {
        // Arrange
        Note emptyNote = new Note();
        emptyNote.setTitle("");
        emptyNote.setBody("");

        // Act
        List<Tag> tags = aiOrganizer.suggestTags(emptyNote);

        // Assert
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    @DisplayName("suggestTags() - Returns existing tags from repository")
    void test_suggestTags_existingTags_returnsFromRepository() {
        // Arrange
        when(tagRepository.findByNameIgnoreCase(anyString()))
            .thenReturn(Optional.of(testTag));

        // Act
        List<Tag> tags = aiOrganizer.suggestTags(testNote);

        // Assert
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        verify(tagRepository, atLeastOnce()).findByNameIgnoreCase(anyString());
    }

    @Test
    @DisplayName("suggestTags() - Creates new tags when not found")
    void test_suggestTags_newTags_createsAndSaves() {
        // Arrange
        when(tagRepository.findByNameIgnoreCase(anyString()))
            .thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
            .thenReturn(testTag);

        // Act
        List<Tag> tags = aiOrganizer.suggestTags(testNote);

        // Assert
        assertNotNull(tags);
        verify(tagRepository, atLeastOnce()).save(any(Tag.class));
    }

    @Test
    @DisplayName("suggestTags() - Note with only title gets tags")
    void test_suggestTags_titleOnly_returnsTags() {
        // Arrange
        Note noteWithTitleOnly = new Note();
        noteWithTitleOnly.setTitle("Work meeting");
        noteWithTitleOnly.setBody(null);

        when(tagRepository.findByNameIgnoreCase(anyString()))
            .thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
            .thenReturn(testTag);

        // Act
        List<Tag> tags = aiOrganizer.suggestTags(noteWithTitleOnly);

        // Assert
        assertNotNull(tags);
    }

    // ==================== categorize() Tests ====================
    @Test
    @DisplayName("categorize() - Null note throws IllegalArgumentException")
    void test_categorize_nullNote_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.categorize(null)
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("categorize() - Valid note returns category")
    void test_categorize_validNote_returnsCategory() {
        // Act
        String category = aiOrganizer.categorize(testNote);

        // Assert
        assertNotNull(category);
        assertFalse(category.isEmpty());
    }

    @Test
    @DisplayName("categorize() - Work note returns Work category")
    void test_categorize_workNote_returnsWorkCategory() {
        // Act
        String category = aiOrganizer.categorize(testNote);

        // Assert
        assertEquals("Work", category);
    }

    @Test
    @DisplayName("categorize() - Health note returns Health category")
    void test_categorize_healthNote_returnsHealthCategory() {
        // Arrange
        Note healthNote = new Note();
        healthNote.setTitle("Doctor appointment");
        healthNote.setBody("Annual health checkup");

        // Act
        String category = aiOrganizer.categorize(healthNote);

        // Assert
        assertEquals("Health", category);
    }

    @Test
    @DisplayName("categorize() - Generic note returns General category")
    void test_categorize_genericNote_returnsGeneralCategory() {
        // Arrange
        Note genericNote = new Note();
        genericNote.setTitle("Random thoughts");
        genericNote.setBody("Just some random notes");

        // Act
        String category = aiOrganizer.categorize(genericNote);

        // Assert
        assertEquals("General", category);
    }

    @Test
    @DisplayName("categorize() - Empty note returns General category")
    void test_categorize_emptyNote_returnsGeneralCategory() {
        // Arrange
        Note emptyNote = new Note();
        emptyNote.setTitle("");
        emptyNote.setBody("");

        // Act
        String category = aiOrganizer.categorize(emptyNote);

        // Assert
        assertEquals("General", category);
    }

    // ==================== categorizeWithUserCategories() Tests ====================
    @Test
    @DisplayName("categorizeWithUserCategories() - Null note throws IllegalArgumentException")
    void test_categorizeWithUserCategories_nullNote_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.categorizeWithUserCategories(null, "user-1")
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("categorizeWithUserCategories() - No user categories returns null")
    void test_categorizeWithUserCategories_noCategories_returnsNull() {
        // Arrange
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(new ArrayList<>());

        // Act
        String category = aiOrganizer.categorizeWithUserCategories(testNote, "user-1");

        // Assert
        assertNull(category);
    }

    @Test
    @DisplayName("categorizeWithUserCategories() - Matches user category")
    void test_categorizeWithUserCategories_matchesCategory_returnsCategory() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(categories);

        // Act
        String category = aiOrganizer.categorizeWithUserCategories(testNote, "user-1");

        // Assert
        assertEquals("Work", category);
    }

    @Test
    @DisplayName("categorizeWithUserCategories() - No match returns null")
    void test_categorizeWithUserCategories_noMatch_returnsNull() {
        // Arrange
        Category unmatchedCategory = new Category();
        unmatchedCategory.setName("Cooking");
        unmatchedCategory.setDescription("recipes food meals");

        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(Arrays.asList(unmatchedCategory));

        // Act
        String category = aiOrganizer.categorizeWithUserCategories(testNote, "user-1");

        // Assert
        assertNull(category);
    }

    // ==================== suggestTagsFromUserCategories() Tests ====================
    @Test
    @DisplayName("suggestTagsFromUserCategories() - Null note throws IllegalArgumentException")
    void test_suggestTagsFromUserCategories_nullNote_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.suggestTagsFromUserCategories(null, "user-1")
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - No categories returns empty list")
    void test_suggestTagsFromUserCategories_noCategories_returnsEmptyList() {
        // Arrange
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(new ArrayList<>());

        // Act
        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(testNote, "user-1");

        // Assert
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - Empty note returns empty list")
    void test_suggestTagsFromUserCategories_emptyNote_returnsEmptyList() {
        // Arrange
        Note emptyNote = new Note();
        emptyNote.setTitle("");
        emptyNote.setBody("");

        // Act
        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(emptyNote, "user-1");

        // Assert
        assertTrue(tags.isEmpty());
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - Matching category creates tag")
    void test_suggestTagsFromUserCategories_matchingCategory_createsTag() {
        // Arrange
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(Arrays.asList(testCategory));
        when(tagRepository.findByNameIgnoreCase("Work"))
            .thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
            .thenReturn(testTag);

        // Act
        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(testNote, "user-1");

        // Assert
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - Uses existing tag if found")
    void test_suggestTagsFromUserCategories_existingTag_returnsIt() {
        // Arrange
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(Arrays.asList(testCategory));
        when(tagRepository.findByNameIgnoreCase("Work"))
            .thenReturn(Optional.of(testTag));

        // Act
        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(testNote, "user-1");

        // Assert
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        verify(tagRepository, never()).save(any(Tag.class));
    }
}
