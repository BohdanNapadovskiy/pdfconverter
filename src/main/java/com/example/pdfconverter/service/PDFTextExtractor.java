package com.example.pdfconverter.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import com.example.pdfconverter.config.DotenvConfig;
import com.example.pdfconverter.model.AWSPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PDFTextExtractor {

    private DotenvConfig config;

    @Value("${aws.access.key}")
    private String aws_access_key;

    @Value("${aws.secret.key}")
    private String aws_secret_key;

    private final AmazonTextract textractClient;

    public PDFTextExtractor(DotenvConfig config) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(aws_access_key,aws_secret_key);
        this.textractClient = AmazonTextractClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public List<AWSPage> extractTextWithWordIds(Path inputPath) throws IOException {
        log.info("Loading PDF from: " + inputPath.toAbsolutePath());
        AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                .withDocument(new Document()
                        .withBytes(ByteBuffer.wrap(Files.readAllBytes(inputPath))))
                .withFeatureTypes(FeatureType.FORMS, FeatureType.TABLES);

        AnalyzeDocumentResult result = textractClient.analyzeDocument(request);
        log.info("Getting blocks from amazon ");
        List<Block> documentBlocks = result.getBlocks();
        return documentBlocks.stream()
                .filter(block -> block.getBlockType().equals("PAGE"))
                .filter(block -> !block.getRelationships().isEmpty())
                .map(block -> AWSPage.builderPage(block, documentBlocks))
                .collect(Collectors.toList());
    }
}
