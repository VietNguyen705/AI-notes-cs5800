package com.notesapp.decorators;

import com.notesapp.entities.Note;
import com.notesapp.services.AIOrganizer;

/**
 * Decorator Pattern - Concrete Decorator
 *
 * Enriches a note by adding an AI-generated category classification.
 * This decorator analyzes the note content and assigns it to the most appropriate
 * category based on the user's existing categories.
 */
public class CategoryEnrichmentDecorator extends BaseNoteEnrichment {
    private final AIOrganizer aiOrganizer;
    private final String userId;

    public CategoryEnrichmentDecorator(Note note, AIOrganizer aiOrganizer, String userId) {
        super(note);
        this.aiOrganizer = aiOrganizer;
        this.userId = userId;
    }

    @Override
    public Note enrich() {
        // Get AI-suggested category based on user's existing categories
        String category = aiOrganizer.categorizeWithUserCategories(note, userId);

        // Set the category for the note
        note.setCategory(category);

        return note;
    }
}
