package com.docmind;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        PdfExtractor extractor = new PdfExtractor();
        TextChunker chunker = new TextChunker();
        EmbeddingClient embeddingClient = new EmbeddingClient();
        VectorStore vectorStore = new VectorStore();
        GeminiClient geminiClient = new GeminiClient();

        // Step 1: Set up the database
        vectorStore.initialize();

        // Step 2: Ask user for PDF path
        System.out.println("=================================");
        System.out.println("     Welcome to DocMind!        ");
        System.out.println("=================================");
        System.out.print("Enter the full path to your PDF file: ");
        String pdfPath = scanner.nextLine();

        // Step 3: Extract text from PDF
        System.out.println("Reading PDF...");
        String text = extractor.extractText(pdfPath);
        if (text.isEmpty()) {
            System.out.println("Could not extract text. Exiting.");
            return;
        }

        // Step 4: Split into chunks
        System.out.println("Splitting into chunks...");
        List<String> chunks = chunker.splitIntoChunks(text);

        // Step 5: Embed each chunk and save to database
        System.out.println("Generating embeddings... (this may take a minute)");
        vectorStore.clearAll();
        for (int i = 0; i < chunks.size(); i++) {
            double[] embedding = embeddingClient.getEmbedding(chunks.get(i));
            vectorStore.saveChunk(chunks.get(i), embedding);
            System.out.println("Processed chunk " + (i + 1) + " of " + chunks.size());
        }

        // Step 6: Question loop
        System.out.println("\nDocument loaded! You can now ask questions.");
        System.out.println("Type 'exit' to quit.\n");

        while (true) {
            System.out.print("Your question: ");
            String question = scanner.nextLine();

            if (question.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (question.trim().isEmpty()) {
                continue;
            }

            // Embed the question
            double[] questionEmbedding = embeddingClient.getEmbedding(question);

            // Find the 3 most relevant chunks
            List<String> relevantChunks = vectorStore.findSimilarChunks(questionEmbedding, 3);

            // Get answer from Gemini
            System.out.println("\nThinking...");
            String answer = geminiClient.askQuestion(question, relevantChunks);
            System.out.println("\nAnswer: " + answer);
            System.out.println("\n---------------------------------\n");
        }

        scanner.close();
    }
}
