package com.notesapp.decorators;

import com.notesapp.entities.Note;

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
