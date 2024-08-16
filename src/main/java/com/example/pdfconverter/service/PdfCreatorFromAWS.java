package com.example.pdfconverter.service;

import com.amazonaws.services.textract.model.Point;
import com.example.pdfconverter.model.AWSPage;
import com.example.pdfconverter.model.AWSWord;
import com.example.pdfconverter.model.FontInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
public class PdfCreatorFromAWS {


    public void overlayTextOnPDF(String inputPdfPath, String outputPdfPath, List<AWSPage> pageList) throws IOException {
        PDDocument document = PDDocument.load(new File(inputPdfPath));

        for (int i = 0; i < pageList.size(); i++) {
            AWSPage awsPage = pageList.get(i);
            PDPage pdpage = document.getPage(i);
            PDRectangle mediaBox = pdpage.getMediaBox();
            overlayPageTextOnPDF(awsPage, mediaBox, document, pdpage);
        }

        // Save the document
        document.save(outputPdfPath);

        // Close the document
        document.close();

        System.out.println("PDF created successfully!");
    }

    private void overlayPageTextOnPDF(AWSPage page, PDRectangle mediaBox, PDDocument document, PDPage pdpage) {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, pdpage, AppendMode.APPEND, true, true)) {
            List<AWSWord> words = page.getListLines().stream()
                    .map(line -> line.getWords().stream().toList())
                    .flatMap(Collection::stream)
                    .toList();

            for (AWSWord word : words) {
                String textractText = word.getText();
                float actualX = word.getNormalizedX() * mediaBox.getWidth();
                float actualY =  (1 - word.getNormalizedY()) * mediaBox.getHeight();

                float realWidth = calculateRealWidth(word.getPoints(), mediaBox.getWidth());
                float realHeight = calculateRealHeight(word.getPoints(), mediaBox.getHeight());
                FontInfo fontInfo = calculateFontInfo(word.getText(), realWidth, realHeight);
                float yOffset = 0;
//                if (fontInfo.getFontSize() >=10) {
//                    yOffset =  calculateYOffsetForBaseline(fontInfo.getFont(), fontInfo.getFontSize(),realHeight);
//                } else {
                    yOffset =  ((realHeight - fontInfo.getTextHeight()) / 2);
//                }
                actualY = actualY+yOffset;
                // Calculate the font size using the bounding box dimensions
                float scalingFactor = realWidth / fontInfo.getTextWidth() * 100;
                contentStream.beginText();
                contentStream.setFont(fontInfo.getFont(), fontInfo.getFontSize());
                contentStream.newLineAtOffset(actualX, actualY);
                contentStream.showText(textractText);
                contentStream.endText();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private float calculateRealWidth(List<Point> points, float mediaBoxWidth) {
        float minX = points.stream().map(Point::getX).min(Float::compare).orElse(0.0f);
        float maxX = points.stream().map(Point::getX).max(Float::compare).orElse(0.0f);
        return (maxX - minX) * mediaBoxWidth;
    }

    private float calculateRealHeight(List<Point> points, float mediaBoxHeight) {
        float minY = points.stream().map(Point::getY).min(Float::compare).orElse(0.0f);
        float maxY = points.stream().map(Point::getY).max(Float::compare).orElse(0.0f);
        return (maxY - minY) * mediaBoxHeight;
    }

    private float calculateYOffsetForBaseline(PDFont font, float fontSize, float bbHeight) {
        float descent = font.getFontDescriptor().getDescent() / 1000 * fontSize;
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        // Calculate offset to align baseline
        return (bbHeight - textHeight) / 2 + descent;
    }

    private float calculateYOffset(float bbHeight, float textHeight) {
        return (bbHeight - textHeight) / 2;
    }

    private FontInfo calculateFontInfo(String text, float realWidth, float realHeight) throws IOException {
        PDFont regularFont = PDType1Font.HELVETICA;
        PDFont boldFont = PDType1Font.HELVETICA_BOLD;
        // Start with the regular font
        PDFont font = regularFont;
        int fontSize = 9;

        // Calculate initial text dimensions with the base font size
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        // Adjust font size based on the height of the bounding box
        if (textHeight > realHeight) {
            while (textHeight > realHeight && fontSize > 1) {
                fontSize -= 1;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
            }
        } else if (textHeight < realHeight) {
            while (textHeight < realHeight) {
                fontSize += 1;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
            }
        }

        // Check if the text width is significantly smaller than the bounding box width
        if (textWidth < realWidth * 0.8) { // Adjust the threshold as needed
            // Switch to bold font and recalculate dimensions
            font = boldFont;
            textWidth = font.getStringWidth(text) / 1000 * fontSize;
            textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        }

        // Adjust character spacing to fit the text width within the bounding box width
        float characterSpacing = 0;
        if (textWidth < realWidth) {
            float additionalSpace = realWidth - textWidth;
            characterSpacing = additionalSpace / (text.length() - 1); // Distribute space between characters
        } else if (textWidth > realWidth) {
            // If the text is wider than the bounding box, reduce character spacing (up to negative values)
            float overrun = textWidth - realWidth;
            characterSpacing = -overrun / (text.length() - 1);
        }

        // Populate the FontInfo object with calculated values
        FontInfo fontInfo = new FontInfo();
        fontInfo.setFont(font); // Set the font (regular or bold)
        fontInfo.setFontSize(fontSize);
        fontInfo.setTextHeight(textHeight);
        fontInfo.setTextWidth(textWidth);
        fontInfo.setCharacterSpacing(characterSpacing);

        return fontInfo;
    }

}