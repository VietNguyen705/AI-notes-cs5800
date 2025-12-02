package com.notesapp.services;

import com.notesapp.config.AppConstants;
import com.notesapp.entities.Note;
import com.notesapp.entities.TodoItem;
import com.notesapp.entities.User;
import com.notesapp.enums.Priority;
import com.notesapp.enums.TaskStatus;
import com.notesapp.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Singleton service for task generation from notes.
 * Spring's @Service annotation ensures only one instance exists (Singleton pattern).
 */
@Slf4j
@Service
public class TaskGenerator {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private OpenAIService openAIService;

    /**
     * Extracts action items from the given text.
     * Uses OpenAI API if available, otherwise falls back to pattern-based extraction.
     *
     * @param text the text to extract action items from
     * @return list of action items, each containing title and description
     * @throws IllegalArgumentException if text is null or empty
     */
    public List<Map<String, Object>> extractActionItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        List<Map<String, Object>> actionItems;

        if (!openAIService.isAvailable()) {
            actionItems = extractActionItemsByPattern(text);
        } else {
            try {
                String prompt = "Extract action items from this note. Return each task on a new line, " +
                               "starting with a dash (-):\n\n" + text;
                String response = openAIService.callAPI(prompt, AppConstants.OPENAI_MAX_TOKENS_TASKS);
                actionItems = parseActionItemsFromResponse(response);
            } catch (Exception e) {
                log.warn("Failed to extract action items using OpenAI, falling back to pattern matching: {}",
                        e.getMessage());
                actionItems = extractActionItemsByPattern(text);
            }
        }

        return actionItems;
    }

    /**
     * Infers a due date from natural language text.
     * Recognizes phrases like "today", "tomorrow", "next week", and date patterns.
     *
     * @param text the text to parse for due date information
     * @return inferred due date, or null if none can be determined
     */
    public LocalDateTime inferDueDate(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        String lowerText = text.toLowerCase();

        if (lowerText.contains("today")) {
            return getEndOfDay(LocalDateTime.now());
        }
        if (lowerText.contains("tomorrow")) {
            return getEndOfDay(LocalDateTime.now().plusDays(1));
        }
        if (lowerText.contains("next week")) {
            return getEndOfDay(LocalDateTime.now().plusWeeks(1));
        }
        if (lowerText.contains("next month")) {
            return getEndOfDay(LocalDateTime.now().plusMonths(1));
        }

        if (containsDatePattern(text)) {
            return getEndOfDay(LocalDateTime.now().plusDays(7));
        }

        return null;
    }

    /**
     * Infers task priority from text content.
     * Analyzes keywords to determine urgency level.
     *
     * @param text the text to analyze for priority
     * @return inferred priority level (defaults to MEDIUM)
     */
    public Priority inferPriority(String text) {
        if (text == null || text.isEmpty()) {
            return Priority.MEDIUM;
        }

        String lowerText = text.toLowerCase();

        if (isUrgentTask(lowerText)) {
            return Priority.URGENT;
        }

        if (isHighPriorityTask(lowerText)) {
            return Priority.HIGH;
        }

        if (isLowPriorityTask(lowerText)) {
            return Priority.LOW;
        }

        return Priority.MEDIUM;
    }

    /**
     * Generates tasks from a note's content.
     * Extracts action items and creates TodoItem entities with inferred metadata.
     *
     * @param note the note to generate tasks from
     * @param user the user who owns the tasks
     * @return list of created and saved TodoItem entities
     * @throws IllegalArgumentException if note is null
     */
    public List<TodoItem> generateTasks(Note note, User user) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        String content = note.getTitle() + "\n" + (note.getBody() != null ? note.getBody() : "");
        List<Map<String, Object>> actionItems = extractActionItems(content);

        List<TodoItem> tasks = new ArrayList<>();

        for (Map<String, Object> item : actionItems) {
            TodoItem task = createTodoItem(item, note, user);
            tasks.add(taskRepository.save(task));
        }

        return tasks;
    }

    /**
     * Extracts action items using pattern matching and keyword detection.
     * Fallback method when OpenAI API is not available.
     *
     * @param text the text to extract action items from
     * @return list of action items
     */
    private List<Map<String, Object>> extractActionItemsByPattern(String text) {
        List<Map<String, Object>> items = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (isActionItem(line)) {
                items.add(createActionItem(line));
            }
        }

        return items;
    }

    /**
     * Checks if a line represents an action item.
     *
     * @param line the line to check
     * @return true if the line is a bullet point or contains action verbs
     */
    private boolean isActionItem(String line) {
        boolean isBulletPoint = line.matches("^[-*•]\\s+.*") || line.matches("^\\[[ x]\\]\\s+.*");
        boolean hasActionVerb = containsActionVerb(line.toLowerCase());
        return isBulletPoint || hasActionVerb;
    }

    /**
     * Checks if text contains any action verbs.
     *
     * @param lowerText the text to check (should be lowercase)
     * @return true if text contains an action verb
     */
    private boolean containsActionVerb(String lowerText) {
        for (String verb : AppConstants.ACTION_VERBS) {
            if (lowerText.contains(verb)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates an action item map from a line of text.
     *
     * @param line the line to convert
     * @return map containing title and description
     */
    private Map<String, Object> createActionItem(String line) {
        Map<String, Object> item = new HashMap<>();
        String cleanedLine = line.replaceAll("^[-*•\\[\\]x\\s]+", "").trim();
        item.put("title", cleanedLine);
        item.put("description", "");
        return item;
    }

    /**
     * Parses action items from OpenAI API response.
     *
     * @param response the API response text
     * @return list of parsed action items
     */
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

    /**
     * Creates a TodoItem entity from an action item map.
     *
     * @param item the action item data
     * @param note the associated note
     * @param user the task owner
     * @return configured TodoItem entity
     */
    private TodoItem createTodoItem(Map<String, Object> item, Note note, User user) {
        TodoItem task = new TodoItem();
        task.setNoteId(note.getNoteId());
        task.setUser(user);
        task.setTitle((String) item.get("title"));
        task.setDescription((String) item.getOrDefault("description", ""));
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(inferPriority((String) item.get("title")));
        task.setDueDate(inferDueDate((String) item.get("title")));
        return task;
    }

    /**
     * Sets time to end of day (23:59).
     *
     * @param dateTime the date to modify
     * @return date with time set to end of day
     */
    private LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        return dateTime
            .withHour(AppConstants.END_OF_DAY_HOUR)
            .withMinute(AppConstants.END_OF_DAY_MINUTE);
    }

    /**
     * Checks if text contains a date pattern.
     *
     * @param text the text to check
     * @return true if date pattern is found
     */
    private boolean containsDatePattern(String text) {
        Pattern datePattern = Pattern.compile("(by|on|due|before)\\s+(\\d{1,2}[/-]\\d{1,2}([/-]\\d{2,4})?)",
                                             Pattern.CASE_INSENSITIVE);
        Matcher matcher = datePattern.matcher(text);
        return matcher.find();
    }

    /**
     * Checks if text indicates urgent priority.
     *
     * @param lowerText the text to check (should be lowercase)
     * @return true if text contains urgent keywords
     */
    private boolean isUrgentTask(String lowerText) {
        return lowerText.contains("urgent") || lowerText.contains("asap") ||
               lowerText.contains("critical") || lowerText.contains("immediately");
    }

    /**
     * Checks if text indicates high priority.
     *
     * @param lowerText the text to check (should be lowercase)
     * @return true if text contains high priority keywords
     */
    private boolean isHighPriorityTask(String lowerText) {
        return lowerText.contains("important") || lowerText.contains("high priority") ||
               lowerText.contains("must");
    }

    /**
     * Checks if text indicates low priority.
     *
     * @param lowerText the text to check (should be lowercase)
     * @return true if text contains low priority keywords
     */
    private boolean isLowPriorityTask(String lowerText) {
        return lowerText.contains("low priority") || lowerText.contains("whenever") ||
               lowerText.contains("maybe");
    }
}
