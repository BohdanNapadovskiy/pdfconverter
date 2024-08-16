
package example.pdfconverter;

import example.pdfconverter.model.AWSPage;
import example.pdfconverter.service.PdfCreatorFromAWS;
import example.pdfconverter.service.TextExtractor;

import java.io.IOException;
import java.util.List;

public class PdfConverterApplication {

  public static void main(String args[]) {
    try {
      TextExtractor textExtractor = new TextExtractor();
      PdfCreatorFromAWS pdfCreator = new PdfCreatorFromAWS();
      String inputFilePath = "Untitled design (6)-3.pdf";
      List<AWSPage> pageList = textExtractor.extractTextWithWordIds(inputFilePath);
      pdfCreator.overlayTextOnPDF(inputFilePath, "", pageList);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
