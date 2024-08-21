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

    private String text;
    private List<AWSWord> words;

    private AWSLine(Block block) {
        this.normalizedX = block.getGeometry().getBoundingBox().getLeft();
        this.normalizedY = block.getGeometry().getBoundingBox().getTop();
        this.normalizedWidth = block.getGeometry().getBoundingBox().getWidth();
        this.normalizedHeight = block.getGeometry().getBoundingBox().getHeight();
        this.text = block.getText();
    }

    public static AWSLine createLine(Block block, List<Block> documentBlocks) {
        AWSLine line = new AWSLine(block);
        List<AWSWord> words = getWordsFromLine(block, documentBlocks);
        line.setWords(words);
        return line;
    }

    private static List<AWSWord> getWordsFromLine(Block lineBlock, List<Block> documentBlocks) {
        Optional<List<String>> childIds = Optional.ofNullable(lineBlock.getRelationships())
                .map(relationships -> relationships.getFirst().getIds());

        return documentBlocks.stream()
                .filter(block -> childIds.orElse(new ArrayList<>()).contains(block.getId()))
                .filter(block -> "WORD".equals(block.getBlockType()))
                .map(AWSWord::createWord)
                .toList();
    }
}