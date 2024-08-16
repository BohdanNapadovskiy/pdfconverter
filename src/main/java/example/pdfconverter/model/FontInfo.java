package example.pdfconverter.model;

public class FontInfo {
    int fontSize;
    float textHeight;
    float textWidth;
    float characterSpacing;

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public float getTextHeight() {
        return textHeight;
    }

    public void setTextHeight(float textHeight) {
        this.textHeight = textHeight;
    }

    public float getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(float textWidth) {
        this.textWidth = textWidth;
    }

    public void setCharacterSpacing(float characterSpacing) {
        this.characterSpacing = characterSpacing;
    }

    public float getCharacterSpacing() {
        return characterSpacing;
    }
}

