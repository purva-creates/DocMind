package com.docmind;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;

    public List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            System.out.println("No text to split.");
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start += (CHUNK_SIZE - OVERLAP);
        }

        System.out.println("Split into " + chunks.size() + " chunks.");
        return chunks;
    }
}