package com.virtupet;

import java.net.http.*;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;
import org.json.*; // You'll need to add this dependency

public class GeminiClient {
    protected static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    protected static String apiKey = System.getenv("GEMINI_API_KEY");

    public static void call(String[] args) {
        GeminiClient app = new GeminiClient();
        if (app.apiKey == null) {
            throw new IllegalStateException("GEMINI_API_KEY não encontrada nas variáveis de ambiente.");
        }

        try {
            String resposta = app.enviarMensagem("Você é uma inteligencia artificial que funcionara como pet virtual...");
            String cleanResponse = app.parseResponse(resposta);
            System.out.println("Resposta formatada: " + cleanResponse);
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String enviarMensagem(String mensagem) throws IOException, InterruptedException {
        String json = """
                {
                  "contents": [
                    {
                      "role": "user",
                      "parts": [{ "text": "%s" }]
                    }
                  ]
                }
                """.formatted(mensagem);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, BodyHandlers.ofString());

        return response.body();
    }

    public String parseResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Check for errors first
            if (jsonObject.has("error")) {
                return "Erro na API: " + jsonObject.getJSONObject("error").getString("message");
            }

            // Get the first candidate
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            if (candidates.length() == 0) {
                return "Nenhuma resposta recebida da API";
            }

            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");

            // Extract text from all parts
            StringBuilder responseText = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.has("text")) {
                    responseText.append(part.getString("text"));
                }
            }

            return responseText.toString().trim();
        } catch (JSONException e) {
            return "Erro ao processar resposta: " + e.getMessage();
        }
    }
}