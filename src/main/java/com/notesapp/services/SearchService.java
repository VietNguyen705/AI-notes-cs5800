package com.notesapp.services;

import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.repositories.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private NoteRepository noteRepository;

    public List<Note> search(String userId, String query, Map<String, Object> filters) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }

        List<Note> results;

        if (query.trim().isEmpty()) {
            results = noteRepository.findByUserId(userId);
        } else {
            results = searchByText(userId, query);
        }

        if (filters != null && !filters.isEmpty()) {
            results = applyFilters(results, filters);
        }

        return results;
    }

    public List<Note> searchByText(String userId, String query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }

        return noteRepository.searchByText(userId, query);
    }

    public List<Note> filterByTags(String userId, List<Tag> tags) {
        if (tags == null) {
            throw new IllegalArgumentException("Tag list cannot be null");
        }

        if (tags.isEmpty()) {
            return noteRepository.findByUserId(userId);
        }

        return noteRepository.findByTags(tags);
    }

    public List<Note> filterByDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        return noteRepository.findByDateRange(userId, start, end);
    }

    public List<Note> combineFilters(String userId, Map<String, Object> filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters cannot be null");
        }

        List<Note> results = noteRepository.findByUserId(userId);
        return applyFilters(results, filters);
    }

    private List<Note> applyFilters(List<Note> notes, Map<String, Object> filters) {
        List<Note> filtered = new ArrayList<>(notes);

        if (filters.containsKey("category")) {
            String category = (String) filters.get("category");
            filtered = filtered.stream()
                .filter(note -> category.equals(note.getCategory()))
                .collect(Collectors.toList());
        }

        if (filters.containsKey("isPinned")) {
            Boolean isPinned = (Boolean) filters.get("isPinned");
            filtered = filtered.stream()
                .filter(note -> isPinned.equals(note.getIsPinned()))
                .collect(Collectors.toList());
        }

        if (filters.containsKey("tags")) {
            @SuppressWarnings("unchecked")
            List<String> tagNames = (List<String>) filters.get("tags");
            filtered = filtered.stream()
                .filter(note -> note.getTags().stream()
                    .anyMatch(tag -> tagNames.contains(tag.getName())))
                .collect(Collectors.toList());
        }

        if (filters.containsKey("startDate") && filters.containsKey("endDate")) {
            LocalDateTime start = (LocalDateTime) filters.get("startDate");
            LocalDateTime end = (LocalDateTime) filters.get("endDate");
            filtered = filtered.stream()
                .filter(note -> !note.getCreatedAt().isBefore(start) &&
                               !note.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());
        }

        if (filters.containsKey("color")) {
            String color = (String) filters.get("color");
            filtered = filtered.stream()
                .filter(note -> color.equals(note.getColor()))
                .collect(Collectors.toList());
        }

        return filtered;
    }
}
