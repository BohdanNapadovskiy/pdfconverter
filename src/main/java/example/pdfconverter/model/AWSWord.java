package example.pdfconverter.model;

import com.amazonaws.services.textract.model.Point;
import lombok.Getter;


import com.amazonaws.services.textract.model.Block;

import java.util.List;

@Getter
public class AWSWord {
  private float normalizedX; //= 0.2f;
  private float normalizedY; //= 0.3f;
  private float normalizedWidth; //= 0.1f;
  private float normalizedHeight; //= 0.05f;
  private List<Point> points;
  private String text;


  private AWSWord(Block block, AWSPage page) {
    this.normalizedX= block.getGeometry().getBoundingBox().getLeft();
    this.normalizedY = block.getGeometry().getBoundingBox().getTop();
    this.normalizedWidth = block.getGeometry().getBoundingBox().getWidth();
    this.normalizedHeight = block.getGeometry().getBoundingBox().getHeight();

    // Convert normalized coordinates to actual coordinates
//    this.actualX =  normalizedX * page.getPageWidth();
//    this.actualY =  (1 - normalizedY) * page.getPageHeight();  // Y coordinate is inverted for PDFBox
    this.text = block.getText();
    this.points = block.getGeometry().getPolygon();
  }

  public static AWSWord createWord(Block block, AWSPage page) {
    return new AWSWord(block,page);
  }
}
