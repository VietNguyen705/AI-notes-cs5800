package com.notesapp.decorators;

import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.services.AIOrganizer;

import java.util.List;

/**
 * Decorator Pattern - Concrete Decorator
 *
 * Enriches a note by adding AI-generated tags based on note content.
 * This decorator analyzes the note's title and body to suggest relevant tags.
 */
public class TagEnrichmentDecorator extends BaseNoteEnrichment {
    private final AIOrganizer aiOrganizer;
    private final String userId;

    public TagEnrichmentDecorator(Note note, AIOrganizer aiOrganizer, String userId) {
        super(note);
        this.aiOrganizer = aiOrganizer;
        this.userId = userId;
    }

    @Override
    public Note enrich() {
        // Get AI-suggested tags based on user's existing categories
        List<Tag> suggestedTags = aiOrganizer.suggestTagsFromUserCategories(note, userId);

        // Add suggested tags to the note
        for (Tag tag : suggestedTags) {
            note.addTag(tag);
        }

        return note;
    }
}
