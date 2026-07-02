package com.docmind;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfExtractor {

    public String extractText(String filePath) {
        File file = new File(filePath);
        String extractedText = "";

        try {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
            document.close();

        } catch (IOException e) {
            System.out.println("Could not read the PDF file. Check the path: " + filePath);
        }

        return extractedText;
    }
}
