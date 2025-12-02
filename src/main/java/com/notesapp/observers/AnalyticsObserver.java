package com.notesapp.observers;

import com.notesapp.entities.Note;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Observer that tracks analytics and statistics about note activity.
 * Maintains counts of note operations for reporting and insights.
 */
@Component
public class AnalyticsObserver implements NoteObserver {

    private final Map<String, Integer> eventCounts = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AnalyticsObserver() {
        eventCounts.put("CREATE", 0);
        eventCounts.put("UPDATE", 0);
        eventCounts.put("DELETE", 0);
    }

    @Override
    public void update(Note note, String eventType) {
        trackEvent(eventType);
        logAnalytics(note, eventType);
    }

    private void trackEvent(String eventType) {
        eventCounts.put(eventType, eventCounts.getOrDefault(eventType, 0) + 1);
    }

    private void logAnalytics(Note note, String eventType) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[Analytics] " + timestamp + " - " + eventType + " event for note: " + note.getTitle());
        System.out.println("[Analytics] Total events - CREATE: " + eventCounts.get("CREATE") +
                ", UPDATE: " + eventCounts.get("UPDATE") +
                ", DELETE: " + eventCounts.get("DELETE"));
    }

    public Map<String, Integer> getEventCounts() {
        return new HashMap<>(eventCounts);
    }
}
