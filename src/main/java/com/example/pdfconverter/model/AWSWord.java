package com.example.pdfconverter.model;

import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Point;
import lombok.Getter;

import java.util.List;

@Getter
public class AWSWord {
    private float normalizedX;
    private float normalizedY;
    private float normalizedWidth;
    private float normalizedHeight;

    private List<Point> points;
    private String text;

    private AWSWord(Block block) {
        this.normalizedX = block.getGeometry().getBoundingBox().getLeft();
        this.normalizedY = block.getGeometry().getBoundingBox().getTop();
        this.normalizedWidth = block.getGeometry().getBoundingBox().getWidth();
        this.normalizedHeight = block.getGeometry().getBoundingBox().getHeight();
        this.text = block.getText();
        this.points = block.getGeometry().getPolygon();
    }

    public static AWSWord createWord(Block block) {
        return new AWSWord(block);
    }
}