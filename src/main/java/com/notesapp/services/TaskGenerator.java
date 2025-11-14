package com.notesapp.services;

import com.notesapp.entities.Note;
import com.notesapp.entities.TodoItem;
import com.notesapp.entities.User;
import com.notesapp.enums.Priority;
import com.notesapp.enums.TaskStatus;
import com.notesapp.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskGenerator {

    @Autowired
    private TaskRepository taskRepository;

    @Value("${openai.api.key:}")
    private String apiKey;

    private final WebClient webClient;

    public TaskGenerator() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();
    }

    public List<Map<String, Object>> extractActionItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        List<Map<String, Object>> actionItems = new ArrayList<>();

        // If API key is not configured, use pattern-based extraction
        if (apiKey == null || apiKey.isEmpty()) {
            actionItems = extractActionItemsByPattern(text);
        } else {
            // Use OpenAI for more accurate extraction
            try {
                String prompt = "Extract action items from this note. Return each task on a new line, " +
                               "starting with a dash (-):\n\n" + text;
                String response = callOpenAI(prompt);
                actionItems = parseActionItemsFromResponse(response);
            } catch (Exception e) {
                // Fallback to pattern-based extraction
                actionItems = extractActionItemsByPattern(text);
            }
        }

        return actionItems;
    }

    public LocalDateTime inferDueDate(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        String lowerText = text.toLowerCase();

        // Check for relative dates
        if (lowerText.contains("today")) {
            return LocalDateTime.now().withHour(23).withMinute(59);
        }
        if (lowerText.contains("tomorrow")) {
            return LocalDateTime.now().plusDays(1).withHour(23).withMinute(59);
        }
        if (lowerText.contains("next week")) {
            return LocalDateTime.now().plusWeeks(1).withHour(23).withMinute(59);
        }
        if (lowerText.contains("next month")) {
            return LocalDateTime.now().plusMonths(1).withHour(23).withMinute(59);
        }

        // Pattern for specific dates (e.g., "by Dec 15", "on 12/15", "due 2024-12-15")
        Pattern datePattern = Pattern.compile("(by|on|due|before)\\s+(\\d{1,2}[/-]\\d{1,2}([/-]\\d{2,4})?)",
                                             Pattern.CASE_INSENSITIVE);
        Matcher matcher = datePattern.matcher(text);
        if (matcher.find()) {
            // Simple date parsing - in production, use proper date parser
            return LocalDateTime.now().plusDays(7); // Default to 1 week
        }

        return null;
    }

    public Priority inferPriority(String text) {
        if (text == null || text.isEmpty()) {
            return Priority.MEDIUM;
        }

        String lowerText = text.toLowerCase();

        // Check for urgency keywords
        if (lowerText.contains("urgent") || lowerText.contains("asap") ||
            lowerText.contains("critical") || lowerText.contains("immediately")) {
            return Priority.URGENT;
        }

        if (lowerText.contains("important") || lowerText.contains("high priority") ||
            lowerText.contains("must")) {
            return Priority.HIGH;
        }

        if (lowerText.contains("low priority") || lowerText.contains("whenever") ||
            lowerText.contains("maybe")) {
            return Priority.LOW;
        }

        return Priority.MEDIUM;
    }

    public List<TodoItem> generateTasks(Note note, User user) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        String content = note.getTitle() + "\n" + (note.getBody() != null ? note.getBody() : "");
        List<Map<String, Object>> actionItems = extractActionItems(content);

        List<TodoItem> tasks = new ArrayList<>();

        for (Map<String, Object> item : actionItems) {
            TodoItem task = new TodoItem();
            task.setNoteId(note.getNoteId());
            task.setUser(user);
            task.setTitle((String) item.get("title"));
            task.setDescription((String) item.getOrDefault("description", ""));
            task.setStatus(TaskStatus.PENDING);
            task.setPriority(inferPriority((String) item.get("title")));
            task.setDueDate(inferDueDate((String) item.get("title")));

            tasks.add(taskRepository.save(task));
        }

        return tasks;
    }

    private List<Map<String, Object>> extractActionItemsByPattern(String text) {
        List<Map<String, Object>> items = new ArrayList<>();

        // Look for action verbs
        String[] actionVerbs = {"buy", "call", "email", "send", "write", "read", "review",
                                "complete", "finish", "prepare", "schedule", "book",
                                "contact", "discuss", "meet", "create", "update", "fix"};

        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String lowerLine = line.toLowerCase();

            // Check if line starts with bullet point or checkbox
            boolean isBulletPoint = line.matches("^[-*•]\\s+.*") || line.matches("^\\[[ x]\\]\\s+.*");

            // Check if line contains action verb
            boolean hasActionVerb = false;
            for (String verb : actionVerbs) {
                if (lowerLine.contains(verb)) {
                    hasActionVerb = true;
                    break;
                }
            }

            if (isBulletPoint || hasActionVerb) {
                Map<String, Object> item = new HashMap<>();
                String cleanedLine = line.replaceAll("^[-*•\\[\\]x\\s]+", "").trim();
                item.put("title", cleanedLine);
                item.put("description", "");
                items.add(item);
            }
        }

        return items;
    }

    private List<Map<String, Object>> parseActionItemsFromResponse(String response) {
        List<Map<String, Object>> items = new ArrayList<>();

        if (response == null || response.isEmpty()) {
            return items;
        }

        String[] lines = response.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String cleanedLine = line.replaceAll("^[-*•\\d+\\.\\)\\s]+", "").trim();
            if (!cleanedLine.isEmpty()) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", cleanedLine);
                item.put("description", "");
                items.add(item);
            }
        }

        return items;
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
        requestBody.put("max_tokens", 200);

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
}
