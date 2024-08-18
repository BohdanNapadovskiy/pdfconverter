package com.example.pdfconverter.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.font.PDFont;

@Getter
@Setter
public class FontInfo {
    private int fontSize;
    private float textHeight;
    private float textWidth;
    private float characterSpacing=0;
    private PDFont font;






}
