package com.notesapp.services;

import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import com.notesapp.entities.Category;
import com.notesapp.repositories.TagRepository;
import com.notesapp.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class AIOrganizer {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${openai.api.key:}")
    private String apiKey;

    private final WebClient webClient;

    public AIOrganizer() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();
    }

    public Map<String, Object> analyzeContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        Map<String, Object> result = new HashMap<>();

        // If API key is not configured, use simple keyword-based analysis
        if (apiKey == null || apiKey.isEmpty()) {
            result.put("tags", extractKeywordTags(text));
            result.put("category", inferCategory(text));
            result.put("sentiment", "neutral");
            return result;
        }

        // Call OpenAI API for advanced analysis
        try {
            String prompt = "Analyze the following note and suggest 3-5 relevant tags and a category. " +
                           "Return only a comma-separated list of tags.\n\nNote: " + text;

            String response = callOpenAI(prompt);
            result.put("tags", parseTagsFromResponse(response));
            result.put("category", inferCategory(text));

        } catch (Exception e) {
            // Fallback to keyword-based analysis
            result.put("tags", extractKeywordTags(text));
            result.put("category", inferCategory(text));
        }

        return result;
    }

    public List<Tag> suggestTags(Note note) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        String content = (note.getTitle() + " " + (note.getBody() != null ? note.getBody() : "")).trim();

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

        String content = (note.getTitle() + " " + (note.getBody() != null ? note.getBody() : "")).trim();

        if (content.isEmpty()) {
            return new ArrayList<>();
        }

        List<Category> userCategories = categoryRepository.findByUserId(userId);

        if (userCategories.isEmpty()) {
            return new ArrayList<>();
        }

        // Create tags from user's category names only
        List<Tag> tags = new ArrayList<>();

        for (Category category : userCategories) {
            // Check if note content matches this category
            String lowerContent = content.toLowerCase();
            String categoryName = category.getName().toLowerCase();
            String categoryDesc = (category.getDescription() != null ? category.getDescription() : "").toLowerCase();

            boolean matches = lowerContent.contains(categoryName);

            for (String word : categoryDesc.split("\\s+")) {
                if (word.length() > 3 && lowerContent.contains(word)) {
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

        String content = (note.getTitle() + " " + (note.getBody() != null ? note.getBody() : "")).trim();
        return inferCategory(content);
    }

    private List<String> extractKeywordTags(String text) {
        String lowerText = text.toLowerCase();
        List<String> tags = new ArrayList<>();

        // Keyword-based tag extraction
        Map<String, List<String>> keywordMap = new HashMap<>();
        keywordMap.put("Work", Arrays.asList("work", "meeting", "project", "deadline", "task", "office", "client"));
        keywordMap.put("Personal", Arrays.asList("personal", "home", "family", "friend", "birthday", "vacation"));
        keywordMap.put("Finance", Arrays.asList("budget", "money", "payment", "invoice", "expense", "bank", "finance"));
        keywordMap.put("Health", Arrays.asList("health", "doctor", "exercise", "gym", "medication", "appointment"));
        keywordMap.put("Shopping", Arrays.asList("buy", "shop", "purchase", "order", "store", "grocery"));
        keywordMap.put("Ideas", Arrays.asList("idea", "brainstorm", "concept", "plan", "think", "consider"));
        keywordMap.put("Study", Arrays.asList("study", "learn", "course", "exam", "homework", "assignment", "class"));

        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    tags.add(entry.getKey());
                    break;
                }
            }
        }

        if (tags.isEmpty()) {
            tags.add("General");
        }

        return tags.stream().distinct().limit(3).toList();
    }

    private String inferCategory(String text) {
        List<String> tags = extractKeywordTags(text);
        return tags.isEmpty() ? "General" : tags.get(0);
    }

    private String callOpenAI(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 100);

        try {
            Mono<Map> response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);

            Map<String, Object> result = response.block();
            if (result != null && result.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
        }

        return "";
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

        return tags.stream().limit(5).toList();
    }

    private String generateRandomColor() {
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"};
        return colors[new Random().nextInt(colors.length)];
    }

    public String categorizeWithUserCategories(Note note, String userId) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        List<Category> userCategories = categoryRepository.findByUserId(userId);

        if (userCategories.isEmpty()) {
            return null;
        }

        String content = (note.getTitle() + " " + (note.getBody() != null ? note.getBody() : "")).trim();

        // Try OpenAI API first if available
        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                String categoryList = userCategories.stream()
                    .map(Category::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

                String prompt = "Given these categories: " + categoryList +
                    "\n\nAnalyze this note and ONLY respond with ONE category name from the list above that best fits it. " +
                    "If none of the categories fit, respond with 'NONE'.\n\nNote: " + content;

                String response = callOpenAI(prompt);
                String trimmedResponse = response.trim();

                // Check if response is one of the valid categories
                for (Category category : userCategories) {
                    if (trimmedResponse.equalsIgnoreCase(category.getName())) {
                        return category.getName();
                    }
                }

                // If OpenAI returned NONE or invalid category, fall back to keyword matching
            } catch (Exception e) {
                System.err.println("Error using OpenAI for categorization: " + e.getMessage());
            }
        }

        // Keyword-based fallback - only from user categories
        String bestCategory = findBestMatchingCategory(content, userCategories);
        return bestCategory;
    }

    private String findBestMatchingCategory(String content, List<Category> categories) {
        String lowerContent = content.toLowerCase();
        Map<String, Integer> categoryScores = new HashMap<>();

        for (Category category : categories) {
            int score = 0;
            String categoryName = category.getName().toLowerCase();
            String categoryDesc = (category.getDescription() != null ? category.getDescription() : "").toLowerCase();

            if (lowerContent.contains(categoryName)) {
                score += 10;
            }

            for (String word : categoryDesc.split("\\s+")) {
                if (word.length() > 3 && lowerContent.contains(word)) {
                    score += 5;
                }
            }

            if (score > 0) {
                categoryScores.put(category.getName(), score);
            }
        }

        // Return null if no keywords match - don't force a category
        if (categoryScores.isEmpty()) {
            return null;
        }

        return categoryScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}
