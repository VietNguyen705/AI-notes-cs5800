package com.notesapp.controllers;

import com.notesapp.decorators.CategoryEnrichmentDecorator;
import com.notesapp.decorators.NoteEnrichment;
import com.notesapp.decorators.SentimentEnrichmentDecorator;
import com.notesapp.decorators.TagEnrichmentDecorator;
import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.entities.User;
import com.notesapp.observers.NoteObserver;
import com.notesapp.repositories.NoteRepository;
import com.notesapp.repositories.UserRepository;
import com.notesapp.services.AIOrganizer;
import com.notesapp.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST controller for note operations.
 * Implements Observer pattern to notify observers of note changes.
 */
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
        System.out.println("✓ NoteController initialized with " + observers.size() + " observers");
    }

    public void registerObserver(NoteObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(NoteObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Note note, String eventType) {
        for (NoteObserver observer : observers) {
            observer.update(note, eventType);
        }
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(@RequestParam String userId) {
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) {
        return noteRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Map<String, Object> noteData) {
        try {
            String userId = (String) noteData.get("userId");
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Note note = new Note();
            note.setUserId(userId);
            note.setUser(user);
            note.setTitle((String) noteData.get("title"));
            note.setBody((String) noteData.getOrDefault("body", ""));
            note.setColor((String) noteData.getOrDefault("color", "#FFFFFF"));
            note.setIsPinned((Boolean) noteData.getOrDefault("isPinned", false));

            note.create();
            Note savedNote = noteRepository.save(note);

            notifyObservers(savedNote, "CREATE");

            return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return noteRepository.findById(id)
            .map(note -> {
                note.update(updates);
                Note updated = noteRepository.save(note);
                notifyObservers(updated, "UPDATE");
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        return noteRepository.findById(id)
            .map(note -> {
                notifyObservers(note, "DELETE");
                note.delete();
                noteRepository.delete(note);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/auto-organize")
    public ResponseEntity<Note> autoOrganizeNote(@PathVariable String id) {
        return noteRepository.findById(id)
            .map(note -> {
                NoteEnrichment tagEnrichment = new TagEnrichmentDecorator(note, aiOrganizer, note.getUserId());
                Note enrichedNote = tagEnrichment.enrich();

                NoteEnrichment categoryEnrichment = new CategoryEnrichmentDecorator(enrichedNote, aiOrganizer, note.getUserId());
                enrichedNote = categoryEnrichment.enrich();

                NoteEnrichment sentimentEnrichment = new SentimentEnrichmentDecorator(enrichedNote);
                enrichedNote = sentimentEnrichment.enrich();

                Note updated = noteRepository.save(enrichedNote);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Note>> searchNotes(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isPinned) {

        Map<String, Object> filters = Map.of();
        if (category != null || isPinned != null) {
            filters = new java.util.HashMap<>();
            if (category != null) filters.put("category", category);
            if (isPinned != null) filters.put("isPinned", isPinned);
        }

        List<Note> results = searchService.search(userId, query, filters);
        return ResponseEntity.ok(results);
    }
}
