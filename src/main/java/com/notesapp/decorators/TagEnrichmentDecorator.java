package com.notesapp.decorators;

import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.services.AIOrganizer;

import java.util.List;

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
        List<Tag> suggestedTags = aiOrganizer.suggestTagsFromUserCategories(note, userId);

        for (Tag tag : suggestedTags) {
            note.addTag(tag);
        }

        return note;
    }
}
