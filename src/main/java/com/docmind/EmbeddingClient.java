package com.docmind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EmbeddingClient {

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL = "gemini-embedding-001";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + MODEL + ":embedContent?key=" + API_KEY;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public double[] getEmbedding(String text) {
        try {
            // Build request body
            JsonObject part = new JsonObject();
            part.addProperty("text", text);

            JsonArray parts = new JsonArray();
            parts.add(part);

            JsonObject content = new JsonObject();
            content.add("parts", parts);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "models/" + MODEL);
            requestBody.add("content", content);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);

            // Check for API error
            if (responseJson.has("error")) {
                System.out.println("API Error: " + responseJson.get("error"));
                return new double[0];
            }

            JsonArray values = responseJson
                    .getAsJsonObject("embedding")
                    .getAsJsonArray("values");

            double[] embedding = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                embedding[i] = values.get(i).getAsDouble();
            }
            return embedding;

        } catch (Exception e) {
            System.out.println("Error getting embedding: " + e.getMessage());
            return new double[0];
        }
    }
}
