package com.example.pdfconverter.model;

import com.amazonaws.services.textract.model.Block;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class AWSLine {
    private float normalizedX;
    private float normalizedY;
    private float normalizedWidth;
    private float normalizedHeight;

    private float actualX;
    private float actualY;
    private float actualWidth;
    private float actualHeight;
    private String text;
    private List<AWSWord> words;

    private AWSLine(Block block, AWSPage page) {
        this.normalizedX = block.getGeometry().getBoundingBox().getLeft();
        this.normalizedY = block.getGeometry().getBoundingBox().getTop();
        this.normalizedWidth = block.getGeometry().getBoundingBox().getWidth();
        this.normalizedHeight = block.getGeometry().getBoundingBox().getHeight();

        // Convert normalized coordinates to actual coordinates
        this.actualX = normalizedX * page.getPageWidth();
        this.actualY = (1 - normalizedY) * page.getPageHeight();
        this.actualWidth = normalizedWidth * page.getPageWidth();
        this.actualHeight = normalizedHeight * page.getPageHeight();
        this.text = block.getText();
    }

    public static AWSLine createLine(Block block, List<Block> documentBlocks, AWSPage page) {
        AWSLine line = new AWSLine(block, page);
        List<AWSWord> words = getWordsFromLine(block, documentBlocks, page);
        line.setWords(words);
        return line;
    }

    private static List<AWSWord> getWordsFromLine(Block lineBlock, List<Block> documentBlocks, AWSPage page) {
        Optional<List<String>> childIds = Optional.ofNullable(lineBlock.getRelationships())
                .map(relationships -> relationships.get(0).getIds());

        return documentBlocks.stream()
                .filter(block -> childIds.orElse(new ArrayList<>()).contains(block.getId()))
                .filter(block -> "WORD".equals(block.getBlockType()))
                .map(block -> AWSWord.createWord(block, page))
                .toList();
    }
}