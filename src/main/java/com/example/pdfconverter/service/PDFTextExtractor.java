package com.example.pdfconverter.service;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.*;
import com.example.pdfconverter.model.AWSPage;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class PDFTextExtractor {

    public List<AWSPage> extractTextWithWordIds(S3Object s3Object, AmazonTextract textractClient) throws IOException {
        ByteBuffer pdfBytes = ByteBuffer.wrap(s3Object.getObjectContent().readAllBytes());

        AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                .withDocument(new Document().withBytes(pdfBytes))
                .withFeatureTypes(FeatureType.FORMS, FeatureType.TABLES);

        AnalyzeDocumentResult result = textractClient.analyzeDocument(request);
        log.info("Getting blocks from Amazon Textract");
        List<Block> documentBlocks = result.getBlocks();
        return documentBlocks.stream()
                .filter(block -> block.getBlockType().equals("PAGE"))
                .filter(block -> !block.getRelationships().isEmpty())
                .map(block -> AWSPage.builderPage(block, documentBlocks))
                .collect(Collectors.toList());
    }


}
