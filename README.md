# DocMind 🧠

A Java-based Retrieval-Augmented Generation (RAG) application that lets you
chat with any PDF document using natural language.

## What it does
- Load any PDF file
- Ask questions about it in plain English
- Get accurate answers grounded in the document's actual content

## How it works
1. Extracts text from PDF using Apache PDFBox
2. Splits text into overlapping chunks
3. Generates semantic embeddings via Gemini API
4. Stores chunks + embeddings in SQLite
5. On each question: embeds the query, finds most similar chunks
   using cosine similarity, sends context to Gemini to generate an answer

## Tech Stack
- Java 17
- Apache PDFBox 2.0.30
- Google Gemini API (embeddings + generation)
- SQLite via JDBC
- Maven

