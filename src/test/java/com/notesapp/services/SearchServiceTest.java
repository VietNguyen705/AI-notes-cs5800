package com.notesapp.services;

import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.repositories.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService Tests")
class SearchServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private SearchService searchService;

    private Note testNote1;
    private Note testNote2;
    private Tag testTag;
    private List<Note> testNotes;

    @BeforeEach
    void setUp() {
        testNote1 = new Note();
        testNote1.setNoteId("note-1");
        testNote1.setUserId("user-1");
        testNote1.setTitle("Meeting Notes");
        testNote1.setBody("Discussion about project requirements");
        testNote1.setCategory("Work");
        testNote1.setColor("#FF0000");
        testNote1.setIsPinned(true);
        testNote1.setCreatedAt(LocalDateTime.now().minusDays(5));

        testNote2 = new Note();
        testNote2.setNoteId("note-2");
        testNote2.setUserId("user-1");
        testNote2.setTitle("Shopping List");
        testNote2.setBody("Buy groceries and supplies");
        testNote2.setCategory("Personal");
        testNote2.setColor("#00FF00");
        testNote2.setIsPinned(false);
        testNote2.setCreatedAt(LocalDateTime.now().minusDays(2));

        testTag = new Tag();
        testTag.setTagId("tag-1");
        testTag.setName("Work");

        testNotes = Arrays.asList(testNote1, testNote2);
    }

    @Test
    @DisplayName("search() - Null query throws IllegalArgumentException")
    void test_search_nullQuery_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchService.search("user-1", null, null)
        );
        assertEquals("Query cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("search() - Empty query returns all user notes")
    void test_search_emptyQuery_returnsAllNotes() {
        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.search("user-1", "", null);

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(noteRepository).findByUserId("user-1");
    }

    @Test
    @DisplayName("search() - Valid query searches by text")
    void test_search_validQuery_searchesByText() {
        when(noteRepository.searchByText("user-1", "meeting"))
            .thenReturn(Arrays.asList(testNote1));

        List<Note> results = searchService.search("user-1", "meeting", null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Meeting Notes", results.get(0).getTitle());
        verify(noteRepository).searchByText("user-1", "meeting");
    }

    @Test
    @DisplayName("search() - With filters applies them to results")
    void test_search_withFilters_appliesFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "Work");

        when(noteRepository.searchByText("user-1", "project"))
            .thenReturn(testNotes);

        List<Note> results = searchService.search("user-1", "project", filters);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Work", results.get(0).getCategory());
    }

    @Test
    @DisplayName("search() - Empty filters returns unfiltered results")
    void test_search_emptyFilters_returnsUnfiltered() {
        when(noteRepository.searchByText("user-1", "test"))
            .thenReturn(testNotes);

        List<Note> results = searchService.search("user-1", "test", new HashMap<>());

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("searchByText() - Null query throws IllegalArgumentException")
    void test_searchByText_nullQuery_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchService.searchByText("user-1", null)
        );
        assertEquals("Query cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("searchByText() - Valid query delegates to repository")
    void test_searchByText_validQuery_delegatesToRepository() {
        when(noteRepository.searchByText("user-1", "meeting"))
            .thenReturn(Arrays.asList(testNote1));

        List<Note> results = searchService.searchByText("user-1", "meeting");

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(noteRepository).searchByText("user-1", "meeting");
    }

    @Test
    @DisplayName("searchByText() - Empty query returns results")
    void test_searchByText_emptyQuery_returnsResults() {
        when(noteRepository.searchByText("user-1", ""))
            .thenReturn(testNotes);

        List<Note> results = searchService.searchByText("user-1", "");

        assertNotNull(results);
    }

    @Test
    @DisplayName("searchByText() - No matches returns empty list")
    void test_searchByText_noMatches_returnsEmptyList() {
        when(noteRepository.searchByText("user-1", "nonexistent"))
            .thenReturn(new ArrayList<>());

        List<Note> results = searchService.searchByText("user-1", "nonexistent");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("filterByTags() - Null tag list throws IllegalArgumentException")
    void test_filterByTags_nullTagList_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchService.filterByTags("user-1", null)
        );
        assertEquals("Tag list cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("filterByTags() - Empty tag list returns all user notes")
    void test_filterByTags_emptyList_returnsAllNotes() {
        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.filterByTags("user-1", new ArrayList<>());

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(noteRepository).findByUserId("user-1");
    }

    @Test
    @DisplayName("filterByTags() - Valid tags filters notes")
    void test_filterByTags_validTags_filtersNotes() {
        List<Tag> tags = Arrays.asList(testTag);
        when(noteRepository.findByTags(tags)).thenReturn(Arrays.asList(testNote1));

        List<Note> results = searchService.filterByTags("user-1", tags);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(noteRepository).findByTags(tags);
    }

    @Test
    @DisplayName("filterByTags() - Multiple tags works correctly")
    void test_filterByTags_multipleTags_works() {
        Tag tag2 = new Tag();
        tag2.setName("Important");
        List<Tag> tags = Arrays.asList(testTag, tag2);

        when(noteRepository.findByTags(tags)).thenReturn(testNotes);

        List<Note> results = searchService.filterByTags("user-1", tags);

        assertNotNull(results);
        verify(noteRepository).findByTags(tags);
    }

    @Test
    @DisplayName("filterByDateRange() - Null start date throws IllegalArgumentException")
    void test_filterByDateRange_nullStartDate_throwsException() {
        LocalDateTime end = LocalDateTime.now();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchService.filterByDateRange("user-1", null, end)
        );
        assertEquals("Start and end dates cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("filterByDateRange() - Null end date throws IllegalArgumentException")
    void test_filterByDateRange_nullEndDate_throwsException() {
        LocalDateTime start = LocalDateTime.now();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchService.filterByDateRange("user-1", start, null)
        );
        assertEquals("Start and end dates cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("filterByDateRange() - Start after end throws IllegalArgumentException")
    void test_filterByDateRange_startAfterEnd_throwsException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchService.filterByDateRange("user-1", start, end)
        );
        assertEquals("Start date must be before end date", exception.getMessage());
    }

    @Test
    @DisplayName("filterByDateRange() - Valid date range filters notes")
    void test_filterByDateRange_validRange_filtersNotes() {
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        when(noteRepository.findByDateRange("user-1", start, end))
            .thenReturn(testNotes);

        List<Note> results = searchService.filterByDateRange("user-1", start, end);

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(noteRepository).findByDateRange("user-1", start, end);
    }

    @Test
    @DisplayName("filterByDateRange() - Same start and end date works")
    void test_filterByDateRange_sameDate_works() {
        LocalDateTime date = LocalDateTime.now();

        when(noteRepository.findByDateRange("user-1", date, date))
            .thenReturn(new ArrayList<>());

        List<Note> results = searchService.filterByDateRange("user-1", date, date);

        assertNotNull(results);
        verify(noteRepository).findByDateRange("user-1", date, date);
    }

    @Test
    @DisplayName("combineFilters() - Null filters throws IllegalArgumentException")
    void test_combineFilters_nullFilters_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchService.combineFilters("user-1", null)
        );
        assertEquals("Filters cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("combineFilters() - Empty filters returns all notes")
    void test_combineFilters_emptyFilters_returnsAllNotes() {
        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", new HashMap<>());

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("combineFilters() - Category filter works")
    void test_combineFilters_categoryFilter_works() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "Work");

        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", filters);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Work", results.get(0).getCategory());
    }

    @Test
    @DisplayName("combineFilters() - isPinned filter works")
    void test_combineFilters_isPinnedFilter_works() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("isPinned", true);

        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", filters);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getIsPinned());
    }

    @Test
    @DisplayName("combineFilters() - Color filter works")
    void test_combineFilters_colorFilter_works() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("color", "#FF0000");

        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", filters);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("#FF0000", results.get(0).getColor());
    }

    @Test
    @DisplayName("combineFilters() - Tags filter works")
    void test_combineFilters_tagsFilter_works() {
        testNote1.getTags().add(testTag);

        Map<String, Object> filters = new HashMap<>();
        filters.put("tags", Arrays.asList("Work"));

        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", filters);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("combineFilters() - Date range filter works")
    void test_combineFilters_dateRangeFilter_works() {
        LocalDateTime start = LocalDateTime.now().minusDays(6);
        LocalDateTime end = LocalDateTime.now().minusDays(4);

        Map<String, Object> filters = new HashMap<>();
        filters.put("startDate", start);
        filters.put("endDate", end);

        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", filters);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("note-1", results.get(0).getNoteId());
    }

    @Test
    @DisplayName("combineFilters() - Multiple filters combine correctly")
    void test_combineFilters_multipleFilters_combinesCorrectly() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "Work");
        filters.put("isPinned", true);

        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", filters);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Work", results.get(0).getCategory());
        assertTrue(results.get(0).getIsPinned());
    }

    @Test
    @DisplayName("combineFilters() - Filters with no matches returns empty list")
    void test_combineFilters_noMatches_returnsEmptyList() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("category", "Nonexistent");

        when(noteRepository.findByUserId("user-1")).thenReturn(testNotes);

        List<Note> results = searchService.combineFilters("user-1", filters);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
