package com.virtupet;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;

public class Main {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private String apiKey;
    public static void main(String[] args) {
        Main app = new Main();
        app.apiKey = System.getenv("GEMINI_API_KEY");
        if (app.apiKey == null) {
            throw new IllegalStateException("GEMINI_API_KEY não encontrada nas variáveis de ambiente.");
        }

        try {
            String resposta = app.enviarMensagem("Olá, mundo!");
            System.out.println("Resposta da API: " + resposta);
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
}
