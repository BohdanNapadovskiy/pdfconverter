package com.example.pdfconverter.service;

import com.example.pdfconverter.model.FontInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class FontService {


    private int fontSize =10;


    public FontInfo calculateFontInfo(String text, float realWidth, float realHeight) throws IOException {
        PDFont regularFont = PDType1Font.HELVETICA;
        PDFont boldFont = PDType1Font.HELVETICA_BOLD;
        // Start with the regular font
        PDFont font = regularFont;
        float scalingFactor =0;
        float characterSpacing =0;

        // Calculate initial text dimensions with the base font size
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        // Adjust font size based on the height of the bounding box
        if (textWidth > realWidth) {
            while (textWidth > realWidth && fontSize > 1) {
                fontSize -= 1;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
            }
        } else if (textWidth < realWidth) {
            while (textWidth < realWidth) {
                fontSize += 1;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
            }
        }

        // Populate the FontInfo object with calculated values
        FontInfo fontInfo = new FontInfo();
        fontInfo.setFont(font); // Set the font (regular or bold)
        fontInfo.setFontSize(fontSize);
        fontInfo.setTextHeight(textHeight);
        fontInfo.setTextWidth(textWidth);
        fontInfo.setScalingFactor(scalingFactor);
        fontInfo.setCharacterSpacing(characterSpacing);

        return fontInfo;
    }

//    @SneakyThrows
//    private FontInfo calculateFontInfoByFontList(PDFont font, String text, float realWidth, float realHeight) {
//        log.info("Calculate font size for font {} for text {}", font.getName() , text );
//        // Calculate initial text dimensions with the base font size
//        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
//        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
//        float scalingFactor =0;
//        float characterSpacing =0;
//        // Adjust font size based on the height of the bounding box
//        if (textWidth > realWidth) {
//            while (textWidth > realWidth && fontSize > 1) {
//                fontSize -= 1;
//                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
//                textWidth = font.getStringWidth(text) / 1000 * fontSize;
//            }
//        } else if (textWidth < realWidth) {
//            while (textWidth < realWidth) {
//                fontSize += 1;
//                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
//                textWidth = font.getStringWidth(text) / 1000 * fontSize;
//            }
//        }
//
//        // Populate the FontInfo object with calculated values
//        FontInfo fontInfo = new FontInfo();
//        fontInfo.setFont(font); // Set the font (regular or bold)
//        fontInfo.setFontSize(fontSize);
//        fontInfo.setTextHeight(textHeight);
//        fontInfo.setTextWidth(textWidth);
//        fontInfo.setCharacterSpacing(characterSpacing);
//
//        fontInfo.setScalingFactor(scalingFactor);
//
//        return fontInfo;
//
//    }

//
//    public List<FontInfo> calculateFontInfoForAllFonts(String text, float realWidth, float realHeight) {
//        List<PDType1Font> fontList = new ArrayList<>();
//        fontList.add(PDType1Font.COURIER);
//        fontList.add(PDType1Font.COURIER_BOLD);
//        fontList.add(PDType1Font.COURIER_BOLD_OBLIQUE);
//        fontList.add(PDType1Font.COURIER_OBLIQUE);
//        fontList.add(PDType1Font.HELVETICA);
//        fontList.add(PDType1Font.HELVETICA_BOLD);
//        fontList.add(PDType1Font.HELVETICA_BOLD_OBLIQUE);
//        fontList.add(PDType1Font.HELVETICA_OBLIQUE);
////        fontList.add(PDType1Font.SYMBOL);
//        fontList.add(PDType1Font.TIMES_ROMAN);
//        fontList.add(PDType1Font.TIMES_BOLD);
//        fontList.add(PDType1Font.TIMES_BOLD_ITALIC);
//        fontList.add(PDType1Font.TIMES_ITALIC);
////        fontList.add(PDType1Font.ZAPF_DINGBATS);
//
//        return fontList.stream()
//                .map(font -> calculateFontInfoByFontList(font, text, realWidth, realHeight))
//                .toList();
//    }


}
