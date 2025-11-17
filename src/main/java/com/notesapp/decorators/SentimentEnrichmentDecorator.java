package com.notesapp.decorators;

import com.notesapp.entities.Note;

import java.util.Arrays;
import java.util.List;

public class SentimentEnrichmentDecorator extends BaseNoteEnrichment {

    private static final List<String> POSITIVE_KEYWORDS = Arrays.asList(
        "happy", "great", "excellent", "good", "love", "wonderful", "amazing",
        "success", "accomplished", "excited", "joy", "fantastic", "awesome"
    );

    private static final List<String> NEGATIVE_KEYWORDS = Arrays.asList(
        "sad", "bad", "terrible", "awful", "hate", "problem", "issue",
        "failed", "disappointed", "worried", "angry", "frustrated", "difficult"
    );

    public SentimentEnrichmentDecorator(Note note) {
        super(note);
    }

    @Override
    public Note enrich() {
        String content = (note.getTitle() + " " + note.getBody()).toLowerCase();

        long positiveCount = POSITIVE_KEYWORDS.stream()
            .filter(content::contains)
            .count();

        long negativeCount = NEGATIVE_KEYWORDS.stream()
            .filter(content::contains)
            .count();

        String sentiment;
        if (positiveCount > negativeCount) {
            sentiment = "positive";
        } else if (negativeCount > positiveCount) {
            sentiment = "negative";
        } else {
            sentiment = "neutral";
        }

        note.setCategory(note.getCategory() + " [" + sentiment + " sentiment]");

        return note;
    }
}
