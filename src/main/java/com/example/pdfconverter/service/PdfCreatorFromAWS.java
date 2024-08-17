package com.example.pdfconverter.service;

import com.amazonaws.services.textract.model.Point;
import com.example.pdfconverter.model.AWSPage;
import com.example.pdfconverter.model.AWSWord;
import com.example.pdfconverter.model.FontInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfCreatorFromAWS {

    private final FontService fontService;


    public void overlayTextOnPDF(String inputPdfPath, Path outputPdfPath, List<AWSPage> pageList) throws IOException {
        log.info("Creating pdf text search pdf file: {}", outputPdfPath);
        PDDocument document = PDDocument.load(new File(inputPdfPath));
        for (int i = 0; i < pageList.size(); i++) {
            log.info("Adding the page  # {}", i+1);
            AWSPage awsPage = pageList.get(i);
            PDPage pdpage = document.getPage(i);
            PDRectangle mediaBox = pdpage.getMediaBox();
            overlayPageTextOnPDF(awsPage, mediaBox, document, pdpage);
        }
        // Save the document
        document.save(outputPdfPath.toFile());
        // Close the document
        document.close();

        log.info("PDF created successfully!");
    }

    private void overlayPageTextOnPDF(AWSPage page, PDRectangle mediaBox, PDDocument document, PDPage pdpage) {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, pdpage, AppendMode.APPEND, true, true)) {
            List<AWSWord> words = page.getListLines().stream()
                    .map(line -> line.getWords().stream().toList())
                    .flatMap(Collection::stream)
                    .toList();

            for (AWSWord word : words) {
                log.info("Process word {}", word.getText());
                processWord(word,mediaBox,contentStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processWord(AWSWord word, PDRectangle mediaBox, PDPageContentStream contentStream) throws IOException {
        String textractText = word.getText();
        float actualX = calculateXOffset(word, mediaBox);
        float actualY = calculateYOffset(word, mediaBox);

        FontInfo fontInfo = calculateFontInfo(word, mediaBox);
        actualY = adjustYPosition(word, fontInfo, mediaBox, actualY);

        contentStream.setRenderingMode(RenderingMode.NEITHER);
        contentStream.beginText();
        contentStream.setFont(fontInfo.getFont(), fontInfo.getFontSize());
        contentStream.newLineAtOffset(actualX, actualY);
        contentStream.showText(textractText);
        contentStream.endText();
    }

    private float adjustYPosition(AWSWord word, FontInfo fontInfo, PDRectangle mediaBox, float actualY) {
        float realHeight = calculateRealHeight(word.getPoints(), mediaBox.getHeight());
        float yOffset = 0;
        if (fontInfo.getFontSize() > 12) {
            yOffset = calculateYOffsetForBaseline(fontInfo.getFont(), fontInfo.getFontSize(), word.getNormalizedHeight());
        } else {
            yOffset = (realHeight - fontInfo.getTextHeight()) / 2;
        }
        return actualY += yOffset;
    }

    private FontInfo calculateFontInfo(AWSWord word, PDRectangle mediaBox) throws IOException {
        float realWidth = calculateRealWidth(word.getPoints(), mediaBox.getWidth());
        float realHeight = calculateRealHeight(word.getPoints(), mediaBox.getHeight());
        return fontService.calculateFontInfo(word.getText(), realWidth, realHeight);
    }

    private float calculateXOffset(AWSWord word, PDRectangle mediaBox) {
        return word.getNormalizedX() * mediaBox.getWidth();
    }

    private float calculateYOffset(AWSWord word, PDRectangle mediaBox) {
        return (1 - word.getNormalizedY()) * mediaBox.getHeight();
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

}