package com.notesapp.observers;

import com.notesapp.entities.Note;
import org.springframework.stereotype.Component;

/**
 * Observer that updates search index when notes change.
 * In a production system, this would rebuild search indices or notify a search service.
 */
@Component
public class SearchIndexObserver implements NoteObserver {

    @Override
    public void update(Note note, String eventType) {
        switch (eventType) {
            case "CREATE":
                indexNote(note);
                break;
            case "UPDATE":
                reindexNote(note);
                break;
            case "DELETE":
                removeFromIndex(note);
                break;
            default:
                System.err.println("Unknown event type: " + eventType);
        }
    }

    private void indexNote(Note note) {
        System.out.println("[SearchIndex] Indexing new note: " + note.getTitle());
    }

    private void reindexNote(Note note) {
        System.out.println("[SearchIndex] Reindexing updated note: " + note.getTitle());
    }

    private void removeFromIndex(Note note) {
        System.out.println("[SearchIndex] Removing note from index: " + note.getTitle());
    }
}
