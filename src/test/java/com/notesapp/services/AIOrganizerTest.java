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

    @Test
    @DisplayName("analyzeContent() - Valid text returns analysis result")
    void test_analyzeContent_validText_returnsAnalysis() {
        String text = "I need to finish the project report by tomorrow";

        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        assertNotNull(result);
        assertTrue(result.containsKey("tags"));
        assertTrue(result.containsKey("category"));
        assertNotNull(result.get("tags"));
        assertNotNull(result.get("category"));
    }

    @Test
    @DisplayName("analyzeContent() - Null text throws IllegalArgumentException")
    void test_analyzeContent_nullText_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.analyzeContent(null)
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("analyzeContent() - Empty text throws IllegalArgumentException")
    void test_analyzeContent_emptyText_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.analyzeContent("   ")
        );
        assertEquals("Text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("analyzeContent() - Work-related text returns Work tag")
    void test_analyzeContent_workText_returnsWorkTag() {
        String text = "Meeting with client about the project deadline";

        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("Work"));
    }

    @Test
    @DisplayName("analyzeContent() - Health-related text returns Health tag")
    void test_analyzeContent_healthText_returnsHealthTag() {
        String text = "Doctor appointment for annual health checkup";

        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("Health"));
    }

    @Test
    @DisplayName("analyzeContent() - Shopping text returns Shopping tag")
    void test_analyzeContent_shoppingText_returnsShoppingTag() {
        String text = "Buy groceries from the store";

        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("Shopping"));
    }

    @Test
    @DisplayName("analyzeContent() - Generic text returns General tag")
    void test_analyzeContent_genericText_returnsGeneralTag() {
        String text = "Random note about something";

        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.contains("General"));
    }

    @Test
    @DisplayName("analyzeContent() - Returns limited number of tags")
    void test_analyzeContent_limitsTagCount() {
        String text = "Work meeting project client study learn homework";

        Map<String, Object> result = aiOrganizer.analyzeContent(text);

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertTrue(tags.size() <= 3);
    }

    @Test
    @DisplayName("suggestTags() - Null note throws IllegalArgumentException")
    void test_suggestTags_nullNote_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.suggestTags(null)
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("suggestTags() - Empty note returns empty list")
    void test_suggestTags_emptyNote_returnsEmptyList() {
        Note emptyNote = new Note();
        emptyNote.setTitle("");
        emptyNote.setBody("");

        List<Tag> tags = aiOrganizer.suggestTags(emptyNote);

        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    @DisplayName("suggestTags() - Returns existing tags from repository")
    void test_suggestTags_existingTags_returnsFromRepository() {
        when(tagRepository.findByNameIgnoreCase(anyString()))
            .thenReturn(Optional.of(testTag));

        List<Tag> tags = aiOrganizer.suggestTags(testNote);

        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        verify(tagRepository, atLeastOnce()).findByNameIgnoreCase(anyString());
    }

    @Test
    @DisplayName("suggestTags() - Creates new tags when not found")
    void test_suggestTags_newTags_createsAndSaves() {
        when(tagRepository.findByNameIgnoreCase(anyString()))
            .thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
            .thenReturn(testTag);

        List<Tag> tags = aiOrganizer.suggestTags(testNote);

        assertNotNull(tags);
        verify(tagRepository, atLeastOnce()).save(any(Tag.class));
    }

    @Test
    @DisplayName("suggestTags() - Note with only title gets tags")
    void test_suggestTags_titleOnly_returnsTags() {
        Note noteWithTitleOnly = new Note();
        noteWithTitleOnly.setTitle("Work meeting");
        noteWithTitleOnly.setBody(null);

        when(tagRepository.findByNameIgnoreCase(anyString()))
            .thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
            .thenReturn(testTag);

        List<Tag> tags = aiOrganizer.suggestTags(noteWithTitleOnly);

        assertNotNull(tags);
    }

    @Test
    @DisplayName("categorize() - Null note throws IllegalArgumentException")
    void test_categorize_nullNote_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.categorize(null)
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("categorize() - Valid note returns category")
    void test_categorize_validNote_returnsCategory() {
        String category = aiOrganizer.categorize(testNote);

        assertNotNull(category);
        assertFalse(category.isEmpty());
    }

    @Test
    @DisplayName("categorize() - Work note returns Work category")
    void test_categorize_workNote_returnsWorkCategory() {
        String category = aiOrganizer.categorize(testNote);

        assertEquals("Work", category);
    }

    @Test
    @DisplayName("categorize() - Health note returns Health category")
    void test_categorize_healthNote_returnsHealthCategory() {
        Note healthNote = new Note();
        healthNote.setTitle("Doctor appointment");
        healthNote.setBody("Annual health checkup");

        String category = aiOrganizer.categorize(healthNote);

        assertEquals("Health", category);
    }

    @Test
    @DisplayName("categorize() - Generic note returns General category")
    void test_categorize_genericNote_returnsGeneralCategory() {
        Note genericNote = new Note();
        genericNote.setTitle("Random thoughts");
        genericNote.setBody("Just some random notes");

        String category = aiOrganizer.categorize(genericNote);

        assertEquals("General", category);
    }

    @Test
    @DisplayName("categorize() - Empty note returns General category")
    void test_categorize_emptyNote_returnsGeneralCategory() {
        Note emptyNote = new Note();
        emptyNote.setTitle("");
        emptyNote.setBody("");

        String category = aiOrganizer.categorize(emptyNote);

        assertEquals("General", category);
    }

    @Test
    @DisplayName("categorizeWithUserCategories() - Null note throws IllegalArgumentException")
    void test_categorizeWithUserCategories_nullNote_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.categorizeWithUserCategories(null, "user-1")
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("categorizeWithUserCategories() - No user categories returns null")
    void test_categorizeWithUserCategories_noCategories_returnsNull() {
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(new ArrayList<>());

        String category = aiOrganizer.categorizeWithUserCategories(testNote, "user-1");

        assertNull(category);
    }

    @Test
    @DisplayName("categorizeWithUserCategories() - Matches user category")
    void test_categorizeWithUserCategories_matchesCategory_returnsCategory() {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(categories);

        String category = aiOrganizer.categorizeWithUserCategories(testNote, "user-1");

        assertEquals("Work", category);
    }

    @Test
    @DisplayName("categorizeWithUserCategories() - No match returns null")
    void test_categorizeWithUserCategories_noMatch_returnsNull() {
        Category unmatchedCategory = new Category();
        unmatchedCategory.setName("Cooking");
        unmatchedCategory.setDescription("recipes food meals");

        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(Arrays.asList(unmatchedCategory));

        String category = aiOrganizer.categorizeWithUserCategories(testNote, "user-1");

        assertNull(category);
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - Null note throws IllegalArgumentException")
    void test_suggestTagsFromUserCategories_nullNote_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> aiOrganizer.suggestTagsFromUserCategories(null, "user-1")
        );
        assertEquals("Note cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - No categories returns empty list")
    void test_suggestTagsFromUserCategories_noCategories_returnsEmptyList() {
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(new ArrayList<>());

        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(testNote, "user-1");

        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - Empty note returns empty list")
    void test_suggestTagsFromUserCategories_emptyNote_returnsEmptyList() {
        Note emptyNote = new Note();
        emptyNote.setTitle("");
        emptyNote.setBody("");

        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(emptyNote, "user-1");

        assertTrue(tags.isEmpty());
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - Matching category creates tag")
    void test_suggestTagsFromUserCategories_matchingCategory_createsTag() {
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(Arrays.asList(testCategory));
        when(tagRepository.findByNameIgnoreCase("Work"))
            .thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
            .thenReturn(testTag);

        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(testNote, "user-1");

        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("suggestTagsFromUserCategories() - Uses existing tag if found")
    void test_suggestTagsFromUserCategories_existingTag_returnsIt() {
        when(categoryRepository.findByUserId("user-1"))
            .thenReturn(Arrays.asList(testCategory));
        when(tagRepository.findByNameIgnoreCase("Work"))
            .thenReturn(Optional.of(testTag));

        List<Tag> tags = aiOrganizer.suggestTagsFromUserCategories(testNote, "user-1");

        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        verify(tagRepository, never()).save(any(Tag.class));
    }
}
