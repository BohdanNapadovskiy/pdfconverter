package com.example.pdfconverter.service;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.example.pdfconverter.config.ConfigLoader;
import com.example.pdfconverter.model.AWSPage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class PdfConversionService {

    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String s3BucketName;
    private final String s3ObjectKey;
    private String s3OutPutObjectKey;
    private final String region;

    private AmazonS3 s3Client;
    private AmazonTextract textractClient;


    public PdfConversionService() {
        ConfigLoader configLoader = new ConfigLoader();
        this.awsAccessKey = configLoader.getProperty("aws.access.key");
        this.awsSecretKey = configLoader.getProperty("aws.secret.key");
        this.s3BucketName = configLoader.getProperty("aws.s3.bucket");
        this.s3ObjectKey = configLoader.getProperty("aws.s3.object");
        this.region = configLoader.getProperty("aws.region");

    }

    private void generateNewOutputS3Key(String prefix) {
        // Construct the new output S3 key using the prefix, original S3 key, timestamp, and unique ID
        this.s3OutPutObjectKey = String.format("%s%s_%s_%s_output.pdf",
                prefix,
                s3ObjectKey);
    }

    public void run() throws IOException {
        buildS3Client();
        buildTextractClient();



        S3Object s3Object = s3Client.getObject(s3BucketName, s3ObjectKey);

        PDFTextExtractor pdfTextExtractor = new PDFTextExtractor();
        List<AWSPage> pages = pdfTextExtractor.extractTextWithWordIds(s3Object,textractClient);
        PdfCreatorFromAWS pdfCreator = new PdfCreatorFromAWS();
        pdfCreator.overlayTextOnPDF(this.s3Client, s3BucketName, s3ObjectKey, s3OutPutObjectKey,pages);

    }


    public void runLocal() throws IOException {
        buildTextractClient();

        Path path = Paths.get("files/449088405_10117108817928621_688891803580633890-1 OCRDone2.pdf");
        String outputFileName = path.getFileName().toString();
        PDFTextExtractor pdfTextExtractor = new PDFTextExtractor();
        List<AWSPage> pages = pdfTextExtractor.extractTextWithWordIds(path, this.textractClient);
        PdfCreatorFromAWS pdfCreator = new PdfCreatorFromAWS();
        Path outputPath = Paths.get("files/result/"+outputFileName);
        pdfCreator.overlayTextOnPDF(path.toString(),outputPath ,pages);

    }

    private void buildTextractClient() {
        log.info("Creating the Textract client with credentials");

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey,awsSecretKey);
        this.textractClient = AmazonTextractClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }


    private  void buildS3Client() {
        log.info("Creating the s3 client with credentials");
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey,awsSecretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
   }

}
