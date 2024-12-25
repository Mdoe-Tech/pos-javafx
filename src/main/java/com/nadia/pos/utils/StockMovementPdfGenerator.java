package com.nadia.pos.utils;

import com.nadia.pos.model.StockMovement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class StockMovementPdfGenerator {
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PDType0Font regularFont;
    private PDType0Font boldFont;

    public void generatePdf(StockMovement movement, String outputPath) throws Exception {
        try (PDDocument document = new PDDocument()) {
            // Load fonts
            regularFont = PDType0Font.load(document, new File("src/main/resources/fonts/RobotoMono-Regular.ttf"));
            boldFont = PDType0Font.load(document, new File("src/main/resources/fonts/RobotoMono-Bold.ttf"));

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Add header
                contentStream.beginText();
                contentStream.setFont(boldFont, 18);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Stock Movement Record");
                contentStream.endText();
                yPosition -= LINE_HEIGHT * 2;

                // Add company info
                addText(contentStream, "Company Name: Your Company", MARGIN, yPosition, boldFont, 12);
                yPosition -= LINE_HEIGHT;
                addText(contentStream, "Address: Your Address", MARGIN, yPosition, regularFont, 12);
                yPosition -= LINE_HEIGHT * 2;

                // Add movement details
                String[][] details = {
                        {"Reference Number:", movement.getReferenceNumber()},
                        {"Date:", DATE_FORMATTER.format(movement.getCreatedAt())},
                        {"Movement Type:", movement.getType().toString()},
                        {"Product:", movement.getProduct().getName()},
                        {"Quantity:", String.valueOf(movement.getQuantity())},
                        {"Previous Stock:", String.valueOf(movement.getPreviousStock())},
                        {"New Stock:", String.valueOf(movement.getNewStock())},
                        {"Unit Cost:", movement.getUnitCost().toString()},
                        {"Reason:", movement.getReason()},
                        {"Processed By:", movement.getProcessedBy().getFullName()}
                };

                for (String[] detail : details) {
                    addLabelValuePair(contentStream, detail[0], detail[1], yPosition);
                    yPosition -= LINE_HEIGHT;
                }

                yPosition -= LINE_HEIGHT;

                // Add notes section
                addText(contentStream, "Notes:", MARGIN, yPosition, boldFont, 12);
                yPosition -= LINE_HEIGHT;

                // Split notes into multiple lines if needed
                String notes = movement.getNotes() != null ? movement.getNotes() : "";
                String[] noteLines = wrapText(notes);
                for (String line : noteLines) {
                    addText(contentStream, line, MARGIN, yPosition, regularFont, 12);
                    yPosition -= LINE_HEIGHT;
                }

                // Add footer
                yPosition = MARGIN + LINE_HEIGHT;
                addText(contentStream, "Generated on: " + DATE_FORMATTER.format(java.time.LocalDateTime.now()),
                        MARGIN, yPosition, regularFont, 10);
            }

            document.save(outputPath);
        }
    }

    private void addLabelValuePair(PDPageContentStream contentStream, String label, String value,
                                   float y) throws Exception {
        addText(contentStream, label, StockMovementPdfGenerator.MARGIN, y, boldFont, 12);
        addText(contentStream, value, StockMovementPdfGenerator.MARGIN + 150, y, regularFont, 12);
    }

    private void addText(PDPageContentStream contentStream, String text, float x, float y,
                         PDType0Font font, float fontSize) throws Exception {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private String[] wrapText(String text) {
        if (text == null || text.isEmpty()) {
            return new String[] {""};
        }

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            if (line.length() + word.length() + 1 <= 80) {
                if (!line.isEmpty()) {
                    line.append(" ");
                }
                line.append(word);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }

        if (!line.isEmpty()) {
            lines.add(line.toString());
        }

        return lines.toArray(new String[0]);
    }
}