package com.notesapp.decorators;

import com.notesapp.entities.Note;

/**
 * Decorator Pattern - Component Interface
 *
 * Defines the interface for enriching notes with AI-generated features.
 * Decorators can be stacked to add multiple enrichments to a note.
 */
public interface NoteEnrichment {
    /**
     * Enriches the note with additional AI-generated content
     * @return the enriched note
     */
    Note enrich();
}
