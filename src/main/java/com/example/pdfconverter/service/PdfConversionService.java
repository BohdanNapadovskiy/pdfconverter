package com.example.pdfconverter.service;


import com.example.pdfconverter.model.AWSPage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfConversionService {
    private final PDFTextExtractor pdfTextExtractor;
    private final PdfCreatorFromAWS pdfCreatorFromAWS;

    @Value("${pdf.input.path}")
    private String inputPath;

    @Value("${pdf.output.path.prefix}")
    private String outputPrefix;

    @PostConstruct
    public void processPdf() throws IOException {
        Path path = Paths.get(inputPath);
        String outputFileName = outputPrefix + path.getFileName().toString();
        Path outputPath = path.getParent().resolve(outputFileName);
        List<AWSPage> pageList = pdfTextExtractor.extractTextWithWordIds(path);
        pdfCreatorFromAWS.overlayTextOnPDF(inputPath, outputPath, pageList);
    }
}
