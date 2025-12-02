package com.notesapp.services;

import com.notesapp.config.AppConstants;
import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.entities.Category;
import com.notesapp.repositories.TagRepository;
import com.notesapp.repositories.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Singleton service for AI-powered note organization.
 * Spring's @Service annotation ensures only one instance exists (Singleton pattern).
 */
@Service
@Slf4j
public class AIOrganizer {

    private static final int CATEGORY_NAME_MATCH_SCORE = 10;
    private static final int DESCRIPTION_WORD_MATCH_SCORE = 5;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OpenAIService openAIService;

    public Map<String, Object> analyzeContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        Map<String, Object> result = new HashMap<>();

        if (!openAIService.isAvailable()) {
            result.put("tags", extractKeywordTags(text));
            result.put("category", inferCategory(text));
            result.put("sentiment", "neutral");
            return result;
        }

        try {
            String prompt = "Analyze the following note and suggest 3-5 relevant tags and a category. " +
                           "Return only a comma-separated list of tags.\n\nNote: " + text;

            String response = openAIService.callAPI(prompt, AppConstants.OPENAI_MAX_TOKENS_TAGS);
            result.put("tags", parseTagsFromResponse(response));
            result.put("category", inferCategory(text));

        } catch (Exception e) {
            result.put("tags", extractKeywordTags(text));
            result.put("category", inferCategory(text));
        }

        return result;
    }

    public List<Tag> suggestTags(Note note) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        String content = extractNoteContent(note);

        if (content.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> analysis = analyzeContent(content);
        @SuppressWarnings("unchecked")
        List<String> suggestedTagNames = (List<String>) analysis.get("tags");

        List<Tag> tags = new ArrayList<>();
        for (String tagName : suggestedTagNames) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    newTag.setColor(generateRandomColor());
                    return tagRepository.save(newTag);
                });
            tags.add(tag);
        }

        return tags;
    }

    public List<Tag> suggestTagsFromUserCategories(Note note, String userId) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        String content = extractNoteContent(note);

        if (content.isEmpty()) {
            return new ArrayList<>();
        }

        List<Category> userCategories = categoryRepository.findByUserId(userId);

        if (userCategories.isEmpty()) {
            return new ArrayList<>();
        }

        List<Tag> tags = new ArrayList<>();

        for (Category category : userCategories) {
            String lowerContent = content.toLowerCase();
            String categoryName = category.getName().toLowerCase();
            String categoryDesc = (category.getDescription() != null ? category.getDescription() : "").toLowerCase();

            boolean matches = lowerContent.contains(categoryName);

            for (String word : categoryDesc.split("\\s+")) {
                if (word.length() > AppConstants.MIN_KEYWORD_LENGTH && lowerContent.contains(word)) {
                    matches = true;
                    break;
                }
            }

            if (matches) {
                Tag tag = tagRepository.findByNameIgnoreCase(category.getName())
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(category.getName());
                        newTag.setColor(category.getColor());
                        return tagRepository.save(newTag);
                    });
                tags.add(tag);
            }
        }

        return tags;
    }

    public String categorize(Note note) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        String content = extractNoteContent(note);
        return inferCategory(content);
    }

    public String categorizeWithUserCategories(Note note, String userId) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        List<Category> userCategories = categoryRepository.findByUserId(userId);

        if (userCategories.isEmpty()) {
            return null;
        }

        String content = extractNoteContent(note);

        if (openAIService.isAvailable()) {
            try {
                String categoryList = userCategories.stream()
                    .map(Category::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

                String prompt = "Given these categories: " + categoryList +
                    "\n\nAnalyze this note and ONLY respond with ONE category name from the list above that best fits it. " +
                    "If none of the categories fit, respond with 'NONE'.\n\nNote: " + content;

                String response = openAIService.callAPI(prompt, AppConstants.OPENAI_MAX_TOKENS_TAGS);
                String trimmedResponse = response != null ? response.trim() : "";

                for (Category category : userCategories) {
                    if (trimmedResponse.equalsIgnoreCase(category.getName())) {
                        return category.getName();
                    }
                }

            } catch (Exception e) {
                log.warn("Error using OpenAI for categorization: {}", e.getMessage());
            }
        }

        String bestCategory = findBestMatchingCategory(content, userCategories);
        return bestCategory;
    }

    private String extractNoteContent(Note note) {
        return (note.getTitle() + " " + (note.getBody() != null ? note.getBody() : "")).trim();
    }

    private List<String> extractKeywordTags(String text) {
        String lowerText = text.toLowerCase();
        List<String> tags = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : AppConstants.KEYWORD_CATEGORIES.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    tags.add(entry.getKey());
                    break;
                }
            }
        }

        if (tags.isEmpty()) {
            tags.add(AppConstants.DEFAULT_CATEGORY);
        }

        return tags.stream().distinct().limit(AppConstants.MAX_KEYWORD_TAGS).toList();
    }

    private String inferCategory(String text) {
        List<String> tags = extractKeywordTags(text);
        return tags.isEmpty() ? AppConstants.DEFAULT_CATEGORY : tags.get(0);
    }

    private List<String> parseTagsFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        String[] parts = response.split(",");
        List<String> tags = new ArrayList<>();

        for (String part : parts) {
            String tag = part.trim().replaceAll("[^a-zA-Z0-9\\s]", "");
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }

        return tags.stream().limit(AppConstants.MAX_SUGGESTED_TAGS).toList();
    }

    private String generateRandomColor() {
        return AppConstants.DEFAULT_TAG_COLORS.get(new Random().nextInt(AppConstants.DEFAULT_TAG_COLORS.size()));
    }

    private String findBestMatchingCategory(String content, List<Category> categories) {
        String lowerContent = content.toLowerCase();
        Map<String, Integer> categoryScores = new HashMap<>();

        for (Category category : categories) {
            int score = 0;
            String categoryName = category.getName().toLowerCase();
            String categoryDesc = (category.getDescription() != null ? category.getDescription() : "").toLowerCase();

            if (lowerContent.contains(categoryName)) {
                score += CATEGORY_NAME_MATCH_SCORE;
            }

            for (String word : categoryDesc.split("\\s+")) {
                if (word.length() > AppConstants.MIN_KEYWORD_LENGTH && lowerContent.contains(word)) {
                    score += DESCRIPTION_WORD_MATCH_SCORE;
                }
            }

            if (score > 0) {
                categoryScores.put(category.getName(), score);
            }
        }

        if (categoryScores.isEmpty()) {
            return null;
        }

        return categoryScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}
