package com.notesapp.decorators;

import com.notesapp.entities.Note;

/**
 * Decorator Pattern - Concrete Component
 *
 * Base implementation that returns the note without any enrichment.
 * This serves as the starting point for decorator chaining.
 */
public class BaseNoteEnrichment implements NoteEnrichment {
    protected Note note;

    public BaseNoteEnrichment(Note note) {
        this.note = note;
    }

    @Override
    public Note enrich() {
        return note;
    }
}
