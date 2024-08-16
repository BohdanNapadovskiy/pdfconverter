package example.pdfconverter.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.FeatureType;
import example.pdfconverter.model.AWSPage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TextExtractor {

  private final AmazonTextract textractClient;

  public TextExtractor() {
      BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAQQFWRYLOOKAKFLUI", "OyhJWiA3k4jgNAy9O98Ul4REB1fc9wxyeWmwOGHb");
      this.textractClient = AmazonTextractClientBuilder.standard()
              .withRegion(Regions.US_WEST_2)
              .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
              .build();
  }


  public List<AWSPage> extractTextWithWordIds(String documentPath) throws IOException {
    AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
        .withDocument(new Document()
            .withBytes(ByteBuffer.wrap(Files.readAllBytes(Paths.get(documentPath)))))
        .withFeatureTypes(FeatureType.FORMS, FeatureType.TABLES, FeatureType.SIGNATURES);

    AnalyzeDocumentResult result = textractClient.analyzeDocument(request);
    List<Block> documentBlocks = result.getBlocks();
    return documentBlocks.stream()
            .filter(block -> block.getBlockType().equals("PAGE"))
            .map(block -> AWSPage.builderPage(block, documentBlocks)).toList();
  }

}
