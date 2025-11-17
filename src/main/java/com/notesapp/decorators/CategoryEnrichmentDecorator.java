package com.notesapp.decorators;

import com.notesapp.entities.Note;
import com.notesapp.services.AIOrganizer;

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
        String category = aiOrganizer.categorizeWithUserCategories(note, userId);

        note.setCategory(category);

        return note;
    }
}
