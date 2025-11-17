package com.notesapp.decorators;

import com.notesapp.entities.Note;

public interface NoteEnrichment {
    Note enrich();
}
