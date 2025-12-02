package com.notesapp.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Application-wide constants.
 * Centralizes magic numbers and hardcoded values following Clean Code principles.
 */
public final class AppConstants {

  private AppConstants() {
    // Prevent instantiation
  }

  // AI Configuration
  public static final int MIN_KEYWORD_LENGTH = 3;
  public static final int MAX_SUGGESTED_TAGS = 5;
  public static final int MAX_KEYWORD_TAGS = 3;
  public static final int OPENAI_MAX_TOKENS_TAGS = 100;
  public static final int OPENAI_MAX_TOKENS_TASKS = 200;

  // Time Configuration
  public static final int END_OF_DAY_HOUR = 23;
  public static final int END_OF_DAY_MINUTE = 59;

  // File Upload Limits
  public static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
  public static final long MAX_AUDIO_SIZE_BYTES = 25 * 1024 * 1024; // 25MB

  // PDF Export Configuration
  public static final float PDF_MARGIN = 50f;
  public static final float PDF_TITLE_FONT_SIZE = 18f;
  public static final float PDF_SUBTITLE_FONT_SIZE = 14f;
  public static final float PDF_BODY_FONT_SIZE = 12f;
  public static final float PDF_LINE_HEIGHT = 15f;
  public static final float PDF_PAGE_WIDTH = 595f; // A4 width in points
  public static final float PDF_WRITABLE_WIDTH = PDF_PAGE_WIDTH - (2 * PDF_MARGIN);

  // Tag Colors
  public static final List<String> DEFAULT_TAG_COLORS = Arrays.asList(
      "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
      "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"
  );

  // Keyword Categories for Fallback Tag Extraction
  public static final Map<String, List<String>> KEYWORD_CATEGORIES = Map.of(
      "Work", Arrays.asList("work", "meeting", "project", "deadline", "task", "office", "client"),
      "Personal", Arrays.asList("personal", "home", "family", "friend", "birthday", "vacation"),
      "Finance", Arrays.asList("budget", "money", "payment", "invoice", "expense", "bank", "finance"),
      "Health", Arrays.asList("health", "doctor", "exercise", "gym", "medication", "appointment"),
      "Shopping", Arrays.asList("buy", "shop", "purchase", "order", "store", "grocery"),
      "Ideas", Arrays.asList("idea", "brainstorm", "concept", "plan", "think", "consider"),
      "Study", Arrays.asList("study", "learn", "course", "exam", "homework", "assignment", "class")
  );

  // Action Verbs for Task Extraction
  public static final List<String> ACTION_VERBS = Arrays.asList(
      "buy", "call", "email", "write", "review", "submit", "prepare", "schedule",
      "complete", "finish", "send", "update", "create", "develop", "test", "fix", "check", "research"
  );

  // Default Values
  public static final String DEFAULT_CATEGORY = "General";
  public static final String DEFAULT_TAG_COLOR = "#808080";
}
