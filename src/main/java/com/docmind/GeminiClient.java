package com.docmind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GeminiClient {
    private static final String API_KEY = System.getenv("GROQ_API_KEY");
    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String askQuestion(String question, List<String> relevantChunks) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Answer the question using ONLY the context below.\n\nCONTEXT:\n");
            for (int i = 0; i < relevantChunks.size(); i++) {
                prompt.append("--- Chunk ").append(i + 1).append(" ---\n");
                prompt.append(relevantChunks.get(i)).append("\n\n");
            }
            prompt.append("QUESTION: ").append(question).append("\nANSWER:");

            // Build request in OpenAI format (Groq uses same format)
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt.toString());

            JsonArray messages = new JsonArray();
            messages.add(message);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "llama-3.3-70b-versatile");
            requestBody.add("messages", messages);
            requestBody.addProperty("max_tokens", 500);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);

            if (responseJson.has("error")) {
                return "API Error: " + responseJson.get("error");
            }

            return responseJson
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Sorry, could not generate an answer.";
        }
    }
}
