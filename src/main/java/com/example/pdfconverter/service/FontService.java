package com.example.pdfconverter.service;

import com.example.pdfconverter.model.FontInfo;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FontService {

    @Value("${font_size}")
    private int fontSize;


    public FontInfo calculateFontInfo(String text, float realWidth, float realHeight) throws IOException {
        PDFont regularFont = PDType1Font.HELVETICA;
        PDFont boldFont = PDType1Font.HELVETICA_BOLD;
        // Start with the regular font
        PDFont font = regularFont;

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

        // Populate the FontInfo object with calculated values
        FontInfo fontInfo = new FontInfo();
        fontInfo.setFont(font); // Set the font (regular or bold)
        fontInfo.setFontSize(fontSize);
        fontInfo.setTextHeight(textHeight);
        fontInfo.setTextWidth(textWidth);
//        fontInfo.setCharacterSpacing(characterSpacing);

        return fontInfo;
    }
}
