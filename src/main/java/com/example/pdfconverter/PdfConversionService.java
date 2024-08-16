package com.example.pdfconverter;

import com.example.pdfconverter.model.AWSPage;
import com.example.pdfconverter.service.PDFTextExtractor;
import com.example.pdfconverter.service.PdfCreatorFromAWS;

import java.io.IOException;
import java.util.List;

public class PdfConversionService {

    public static void main(String... args) throws IOException {
        String inputPath = "SampleInput.pdf";
        String outputPath = "output.pdf";
        PDFTextExtractor pdfTextExtractor = new PDFTextExtractor();
        PdfCreatorFromAWS pdfCreatorFromAWS = new PdfCreatorFromAWS();
        List<AWSPage> pageList =  pdfTextExtractor.extractTextWithWordIds(inputPath);
        pdfCreatorFromAWS.overlayTextOnPDF(inputPath, outputPath, pageList);

    }
}
