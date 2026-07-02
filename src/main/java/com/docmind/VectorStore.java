package com.docmind;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VectorStore {

    private static final String DB_URL = "jdbc:sqlite:docmind.db";

    // Sets up the database table when the program first runs
    public void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS chunks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    text TEXT NOT NULL,
                    embedding TEXT NOT NULL
                )
                """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Database ready.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Saves one chunk + its embedding into the database
    public void saveChunk(String text, double[] embedding) {
        String sql = "INSERT INTO chunks (text, embedding) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, text);
            stmt.setString(2, embeddingToString(embedding));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving chunk: " + e.getMessage());
        }
    }

    // Finds the most relevant chunks for a user's question
    public List<String> findSimilarChunks(double[] questionEmbedding, int topK) {
        List<String> results = new ArrayList<>();
        List<double[]> allEmbeddings = new ArrayList<>();
        List<String> allTexts = new ArrayList<>();

        String sql = "SELECT text, embedding FROM chunks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                allTexts.add(rs.getString("text"));
                allEmbeddings.add(stringToEmbedding(rs.getString("embedding")));
            }
        } catch (SQLException e) {
            System.out.println("Error reading chunks: " + e.getMessage());
        }

        // Score each chunk by similarity to the question
        double[] scores = new double[allTexts.size()];
        for (int i = 0; i < allTexts.size(); i++) {
            scores[i] = cosineSimilarity(questionEmbedding, allEmbeddings.get(i));
        }

        // Pick the top K most similar chunks
        for (int k = 0; k < topK && k < allTexts.size(); k++) {
            int bestIndex = 0;
            for (int i = 1; i < scores.length; i++) {
                if (scores[i] > scores[bestIndex]) bestIndex = i;
            }
            results.add(allTexts.get(bestIndex));
            scores[bestIndex] = -1; // mark as used
        }

        return results;
    }

    // Clears the database (used when loading a new document)
    public void clearAll() {
        String sql = "DELETE FROM chunks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error clearing database: " + e.getMessage());
        }
    }

    // Converts a double array to a comma-separated string for storage
    private String embeddingToString(double[] embedding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        return sb.toString();
    }

    // Converts the stored string back to a double array
    private double[] stringToEmbedding(String str) {
        String[] parts = str.split(",");
        double[] embedding = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Double.parseDouble(parts[i]);
        }
        return embedding;
    }

    // The math that measures similarity between two vectors
    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
