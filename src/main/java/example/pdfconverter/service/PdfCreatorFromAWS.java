package example.pdfconverter.service;

import com.amazonaws.services.textract.model.Point;
import example.pdfconverter.model.AWSPage;
import example.pdfconverter.model.AWSWord;
import example.pdfconverter.model.FontInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class PdfCreatorFromAWS {

  private PDFont font = PDType1Font.HELVETICA;

  public void overlayTextOnPDF(String inputPdfPath, String outputPdfPath, List<AWSPage> pageList) throws IOException {
    PDDocument document = PDDocument.load(new File(inputPdfPath));

    for (int i = 0; i < pageList.size(); i++) {
      AWSPage awsPage = pageList.get(i);
      PDPage pdpage = document.getPage(i);
      PDRectangle mediaBox = pdpage.getMediaBox();
      overlayPageTextOnPDF(awsPage, mediaBox, document, pdpage);
    }

    // Save the document
    document.save("output.pdf");

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
        float realWidth = calculateRealWidth(word.getPoints(), mediaBox.getWidth());
        float realHeight = calculateRealHeight(word.getPoints(), mediaBox.getHeight());

        FontInfo fontInfo = calculateFontSizeAndKerning(textractText, realWidth, realHeight, font);

        float actualX = word.getNormalizedX() * mediaBox.getWidth();
        float actualY = ((1 - word.getNormalizedY()) * mediaBox.getHeight());
        float yOffset = calculateYOffset(realHeight, fontInfo.getTextHeight());//-1.4f;
        float yOffset1 = calculateYOffsetForBaseline(font, fontInfo.getFontSize(), word.getNormalizedHeight());//-1.4f;
        float adjustedY = actualY;
        if (fontInfo.getFontSize() > 10) {
          adjustedY = actualY + yOffset1;
        } else if (fontInfo.getFontSize() < 10) {
          adjustedY = actualY + yOffset;
        }

        // Calculate the font size using the bounding box dimensions


        contentStream.setFont(font, fontInfo.getFontSize());
        contentStream.setCharacterSpacing(fontInfo.getCharacterSpacing());
        contentStream.beginText();
        contentStream.newLineAtOffset(actualX, adjustedY);
        contentStream.showText(textractText);
        contentStream.endText();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private FontInfo calculateFontSizeAndKerning(String text, float realWidth, float realHeight, PDFont font) throws IOException {
    // Determine the font size based on the height of the bounding box
    int fontSize = (int) (realHeight * 1000 / font.getFontDescriptor().getFontBoundingBox().getHeight());

    // Calculate the text width with the determined font size
    float textWidth = font.getStringWidth(text) / 1000 * fontSize;
    float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

    // Ensure the text height fits within the bounding box height
    if (textHeight > realHeight) {
      while (textHeight > realHeight && fontSize > 1) {
        fontSize -= 1;
        textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        textWidth = font.getStringWidth(text) / 1000 * fontSize; // Update textWidth as fontSize changes
      }
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
    if((text.length() - 1) == 0) {
      characterSpacing =0;
    }

    FontInfo fi = new FontInfo();
    fi.setFontSize(fontSize);
    fi.setTextHeight(textHeight);
    fi.setTextWidth(textWidth);
    fi.setCharacterSpacing(characterSpacing); // Set character spacing in FontInfo

    return fi;
  }
  private float calculateYOffset(float bbHeight, float textHeight) {
    // Calculate the difference between the bounding box height and the text height
    float yOffset = (bbHeight - textHeight) / 2;

    // Adjust the offset if needed, e.g., if you want the text to align differently
    return yOffset;
  }

  private float calculateYOffsetForBaseline(PDFont font, float fontSize, float bbHeight) {
    float descent = font.getFontDescriptor().getDescent() / 1000 * fontSize;
    float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    // Calculate offset to align baseline
    float yOffset = (bbHeight - textHeight) / 2 + descent;
    return yOffset;
  }

  public float calculateRealWidth(List<Point> polygon, float mediaBoxWidth) {
    // Find the min and max X values in the polygon
    float minX = polygon.stream().map(Point::getX).min(Float::compare).orElse(0.0f);
    float maxX = polygon.stream().map(Point::getX).max(Float::compare).orElse(0.0f);

    // Convert normalized width to actual width
    return (maxX - minX) * mediaBoxWidth;
  }

  public float calculateRealHeight(List<Point> polygon, float mediaBoxHeight) {
    // Find the min and max Y values in the polygon
    float minY = polygon.stream().map(Point::getY).min(Float::compare).orElse(0.0f);
    float maxY = polygon.stream().map(Point::getY).max(Float::compare).orElse(0.0f);

    // Convert normalized height to actual height
    return (maxY - minY) * mediaBoxHeight;
  }
}