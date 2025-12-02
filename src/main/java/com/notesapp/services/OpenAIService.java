package com.notesapp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with OpenAI API.
 * Centralizes API communication to avoid code duplication.
 */
@Service
public class OpenAIService {

  private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
  private static final String MODEL = "gpt-3.5-turbo";
  private static final int DEFAULT_MAX_TOKENS = 100;

  @Value("${openai.api.key:}")
  private String apiKey;

  private final WebClient webClient;

  public OpenAIService() {
    this.webClient = WebClient.builder()
        .baseUrl(OPENAI_API_URL)
        .build();
  }

  /**
   * Checks if OpenAI API is available (API key is configured).
   *
   * @return true if API key is set, false otherwise
   */
  public boolean isAvailable() {
    return apiKey != null && !apiKey.isEmpty();
  }

  /**
   * Calls OpenAI API with the given prompt.
   *
   * @param prompt the prompt to send to OpenAI
   * @return the API response text, or null if the call fails
   */
  public String callAPI(String prompt) {
    return callAPI(prompt, DEFAULT_MAX_TOKENS);
  }

  /**
   * Calls OpenAI API with the given prompt and token limit.
   *
   * @param prompt the prompt to send to OpenAI
   * @param maxTokens maximum tokens for the response
   * @return the API response text, or null if the call fails
   */
  public String callAPI(String prompt, int maxTokens) {
    if (!isAvailable()) {
      return null;
    }

    try {
      Map<String, Object> requestBody = buildRequestBody(prompt, maxTokens);
      String response = executeRequest(requestBody);
      return extractResponseText(response);
    } catch (Exception e) {
      System.err.println("OpenAI API call failed: " + e.getMessage());
      return null;
    }
  }

  private Map<String, Object> buildRequestBody(String prompt, int maxTokens) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", MODEL);
    requestBody.put("messages", List.of(
        Map.of("role", "user", "content", prompt)
    ));
    requestBody.put("max_tokens", maxTokens);
    requestBody.put("temperature", 0.7);
    return requestBody;
  }

  private String executeRequest(Map<String, Object> requestBody) {
    return webClient.post()
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

  private String extractResponseText(String response) {
    if (response == null) {
      return null;
    }

    int contentStart = response.indexOf("\"content\":\"") + 11;
    int contentEnd = response.indexOf("\"", contentStart);

    if (contentStart > 10 && contentEnd > contentStart) {
      return response.substring(contentStart, contentEnd)
          .replace("\\n", "\n")
          .trim();
    }

    return null;
  }
}
