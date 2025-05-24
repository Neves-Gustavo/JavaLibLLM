package com.virtupet;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

public class GeminiClient {
    private final String apiKey;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public GeminiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String sendPrompt(String prompt) throws IOException, InterruptedException {
        String jsonBody = String.format("""
            {
              "contents": [{
                "parts": [{"text": "ACT AS PET ASSISTANT. RESPONSE MUST BE: [SAFE]%s[CMD] "}]
              }]
            }""", prompt);
// Continuation of GeminiClient.java

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error: " + response.statusCode() + " - " + response.body());
        }

        return parseGeminiResponse(response.body());
    }

    private String parseGeminiResponse(String jsonResponse) {
        JSONObject root = new JSONObject(jsonResponse);

        // Error handling
        if (root.has("error")) {
            JSONObject error = root.getJSONObject("error");
            throw new RuntimeException("Gemini Error: " + error.getString("message"));
        }

        // Extract response text with safety fallbacks
        return root.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .replace("\n", " ")  // Sanitize newlines
                .trim();
    }

    // Security-focused response formatter
    public static String formatForCommand(String aiText) {
        return "[SAFE]" +
                aiText.replaceAll("[^a-zA-Z0-9\\s]", "") + // Remove special chars
                "[CMD]" +
                aiText.replaceAll("[^a-zA-Z0-9\\s_]", "");  // Safer command subset
    }
    public String generateReminder(String context) throws Exception {
        String prompt = String.format("""
        Generate reminder with command. Format: [SAFE]<message>[CMD]<command>
        Context: %s
        Safe commands: timeout, start, echo
        Example response: [SAFE]Time to hydrate! 💦[CMD]timeout 5 && start water_reminder.mp3
        """, context);

        try {
            return sendPrompt(prompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}