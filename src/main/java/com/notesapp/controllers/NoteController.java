package com.notesapp.controllers;

import com.notesapp.decorators.CategoryEnrichmentDecorator;
import com.notesapp.decorators.NoteEnrichment;
import com.notesapp.decorators.SentimentEnrichmentDecorator;
import com.notesapp.decorators.TagEnrichmentDecorator;
import com.notesapp.entities.Note;
import com.notesapp.entities.User;
import com.notesapp.observers.NoteObserver;
import com.notesapp.repositories.NoteRepository;
import com.notesapp.repositories.UserRepository;
import com.notesapp.services.AIOrganizer;
import com.notesapp.services.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for note operations.
 * Implements Observer pattern to notify observers of note changes.
 * Uses Decorator pattern for AI-based note enrichment.
 */
@Slf4j
@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AIOrganizer aiOrganizer;

    @Autowired
    private SearchService searchService;

    @Autowired(required = false)
    private List<NoteObserver> observers = new ArrayList<>();

    @PostConstruct
    public void initializeObservers() {
        log.info("NoteController initialized with {} observers", observers.size());
    }

    /**
     * Registers an observer to be notified of note changes.
     *
     * @param observer the observer to register
     */
    public void registerObserver(NoteObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.debug("Registered observer: {}", observer.getClass().getSimpleName());
        }
    }

    /**
     * Removes an observer from the notification list.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(NoteObserver observer) {
        observers.remove(observer);
        log.debug("Removed observer: {}", observer.getClass().getSimpleName());
    }

    /**
     * Notifies all registered observers of a note event.
     *
     * @param note the note that changed
     * @param eventType the type of event (CREATE, UPDATE, DELETE)
     */
    private void notifyObservers(Note note, String eventType) {
        log.debug("Notifying {} observers of {} event for note {}", observers.size(), eventType, note.getId());
        for (NoteObserver observer : observers) {
            try {
                observer.update(note, eventType);
            } catch (Exception e) {
                log.error("Observer {} failed to process {} event", observer.getClass().getSimpleName(), eventType, e);
            }
        }
    }

    /**
     * Retrieves all notes for a user, ordered by creation date descending.
     *
     * @param userId the user ID to filter notes
     * @return list of notes for the user
     */
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(@RequestParam String userId) {
        log.info("Fetching all notes for user: {}", userId);
        try {
            List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.debug("Found {} notes for user {}", notes.size(), userId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Failed to fetch notes for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a single note by ID.
     *
     * @param id the note ID
     * @return the note if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) {
        log.info("Fetching note by id: {}", id);
        return noteRepository.findById(id)
            .map(note -> {
                log.debug("Found note: {}", id);
                return ResponseEntity.ok(note);
            })
            .orElseGet(() -> {
                log.warn("Note not found: {}", id);
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * Creates a new note for a user.
     *
     * @param noteData map containing note properties (userId, title, body, color, isPinned)
     * @return the created note with 201 status, or error response
     */
    @PostMapping
    public ResponseEntity<?> createNote(@RequestBody Map<String, Object> noteData) {
        log.info("Creating new note for user: {}", noteData.get("userId"));
        try {
            String userId = (String) noteData.get("userId");
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("Note creation failed: missing userId");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User ID is required"));
            }

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            Note note = buildNoteFromData(noteData, user);
            note.create();
            Note savedNote = noteRepository.save(note);

            log.info("Created note {} for user {}", savedNote.getId(), userId);
            notifyObservers(savedNote, "CREATE");

            return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid note data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (ClassCastException e) {
            log.warn("Invalid note data format: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid data format"));
        } catch (Exception e) {
            log.error("Failed to create note", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Updates an existing note.
     *
     * @param id the note ID
     * @param updates map of properties to update
     * @return the updated note, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        log.info("Updating note: {}", id);
        try {
            return noteRepository.findById(id)
                .map(note -> {
                    note.update(updates);
                    Note updated = noteRepository.save(note);
                    log.info("Updated note: {}", id);
                    notifyObservers(updated, "UPDATE");
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> {
                    log.warn("Note not found for update: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("Failed to update note {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update note"));
        }
    }

    /**
     * Deletes a note by ID.
     *
     * @param id the note ID
     * @return 200 if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        log.info("Deleting note: {}", id);
        try {
            return noteRepository.findById(id)
                .map(note -> {
                    notifyObservers(note, "DELETE");
                    note.delete();
                    noteRepository.delete(note);
                    log.info("Deleted note: {}", id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> {
                    log.warn("Note not found for deletion: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("Failed to delete note {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Auto-organizes a note using AI enrichment decorators.
     * Applies tag, category, and sentiment analysis in sequence.
     *
     * @param id the note ID
     * @return the enriched note, or 404 if not found
     */
    @PostMapping("/{id}/auto-organize")
    public ResponseEntity<?> autoOrganizeNote(@PathVariable String id) {
        log.info("Auto-organizing note: {}", id);
        try {
            return noteRepository.findById(id)
                .map(note -> {
                    Note enrichedNote = applyEnrichmentDecorators(note);
                    Note updated = noteRepository.save(enrichedNote);
                    log.info("Auto-organized note: {}", id);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> {
                    log.warn("Note not found for auto-organize: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("Failed to auto-organize note {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to auto-organize note"));
        }
    }

    /**
     * Searches notes based on query and filters.
     *
     * @param userId the user ID to search within
     * @param query search query string (optional)
     * @param category filter by category (optional)
     * @param isPinned filter by pinned status (optional)
     * @return list of matching notes
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchNotes(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isPinned) {

        log.info("Searching notes for user {} with query '{}', category '{}', isPinned '{}'",
                userId, query, category, isPinned);
        try {
            Map<String, Object> filters = buildSearchFilters(category, isPinned);
            List<Note> results = searchService.search(userId, query, filters);
            log.debug("Found {} notes matching search criteria", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Search failed for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Search failed"));
        }
    }

    /**
     * Builds a Note entity from request data.
     *
     * @param noteData the request data
     * @param user the user entity
     * @return constructed Note entity
     */
    private Note buildNoteFromData(Map<String, Object> noteData, User user) {
        Note note = new Note();
        note.setUserId(user.getId());
        note.setUser(user);
        note.setTitle((String) noteData.get("title"));
        note.setBody((String) noteData.getOrDefault("body", ""));
        note.setColor((String) noteData.getOrDefault("color", "#FFFFFF"));
        note.setIsPinned((Boolean) noteData.getOrDefault("isPinned", false));
        return note;
    }

    /**
     * Applies enrichment decorators to a note in sequence.
     * Decorator pattern: Tag -> Category -> Sentiment
     *
     * @param note the note to enrich
     * @return the enriched note
     */
    private Note applyEnrichmentDecorators(Note note) {
        NoteEnrichment tagEnrichment = new TagEnrichmentDecorator(note, aiOrganizer, note.getUserId());
        Note enrichedNote = tagEnrichment.enrich();

        NoteEnrichment categoryEnrichment = new CategoryEnrichmentDecorator(enrichedNote, aiOrganizer, note.getUserId());
        enrichedNote = categoryEnrichment.enrich();

        NoteEnrichment sentimentEnrichment = new SentimentEnrichmentDecorator(enrichedNote);
        enrichedNote = sentimentEnrichment.enrich();

        return enrichedNote;
    }

    /**
     * Builds search filters map from optional parameters.
     *
     * @param category optional category filter
     * @param isPinned optional pinned status filter
     * @return map of active filters
     */
    private Map<String, Object> buildSearchFilters(String category, Boolean isPinned) {
        Map<String, Object> filters = new HashMap<>();
        if (category != null) {
            filters.put("category", category);
        }
        if (isPinned != null) {
            filters.put("isPinned", isPinned);
        }
        return filters;
    }
}
