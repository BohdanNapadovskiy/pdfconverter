package com.example.pdfconverter.model;

import com.amazonaws.services.textract.model.Block;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Getter
@Setter
public class AWSPage {

    private float pageWidth;
    private float pageHeight;
    private float pageLeft;
    private float pageTop;
    private List<AWSLine> listLines;

    private AWSPage(Block block) {
        this.pageHeight = block.getGeometry().getBoundingBox().getHeight();
        this.pageWidth = block.getGeometry().getBoundingBox().getWidth();
        this.pageLeft = block.getGeometry().getBoundingBox().getLeft();
        this.pageTop = block.getGeometry().getBoundingBox().getTop();
    }

    public static AWSPage builderPage(Block block, List<Block> documentBlocks) {
        AWSPage page = new AWSPage(block);
        List<AWSLine> lines = getLinesFromPage(block, documentBlocks, page);
        page.setListLines(lines);
        return page;
    }

    private static List<AWSLine> getLinesFromPage(Block pageblock, List<Block> documentBlocks, AWSPage page) {
        Optional<List<String>> childIds = Optional.ofNullable(pageblock.getRelationships())
                .map(relationships -> relationships.getFirst().getIds());

        return documentBlocks.stream()
                .filter(block -> childIds.orElse(new ArrayList<>()).contains(block.getId()))
                .filter(block -> "LINE".equals(block.getBlockType()))
                .map(block -> AWSLine.createLine(block, documentBlocks))
                .toList();
    }
}