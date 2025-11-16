package com.notesapp.decorators;

import com.notesapp.entities.Note;

import java.util.Arrays;
import java.util.List;

/**
 * Decorator Pattern - Concrete Decorator
 *
 * Enriches a note by adding sentiment analysis.
 * Analyzes the note's content to determine if it has a positive, negative, or neutral tone.
 * This is a simple keyword-based implementation for demonstration purposes.
 */
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

        // Count positive and negative keywords
        long positiveCount = POSITIVE_KEYWORDS.stream()
            .filter(content::contains)
            .count();

        long negativeCount = NEGATIVE_KEYWORDS.stream()
            .filter(content::contains)
            .count();

        // Determine sentiment based on keyword counts
        String sentiment;
        if (positiveCount > negativeCount) {
            sentiment = "positive";
        } else if (negativeCount > positiveCount) {
            sentiment = "negative";
        } else {
            sentiment = "neutral";
        }

        // Store sentiment as a tag (since Note entity doesn't have a sentiment field)
        // This is a simple approach - in a real app, you might extend the Note entity
        note.setCategory(note.getCategory() + " [" + sentiment + " sentiment]");

        return note;
    }
}
