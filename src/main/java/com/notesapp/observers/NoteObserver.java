package com.notesapp.observers;

import com.notesapp.entities.Note;

/**
 * Observer pattern interface for note change notifications.
 * Observers implement this interface to receive updates when notes are created, updated, or deleted.
 */
public interface NoteObserver {

    /**
     * Called when a note event occurs.
     *
     * @param note The note that was modified
     * @param eventType Type of event: "CREATE", "UPDATE", or "DELETE"
     */
    void update(Note note, String eventType);
}
