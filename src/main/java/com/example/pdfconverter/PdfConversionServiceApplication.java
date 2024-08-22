package com.example.pdfconverter;

import com.example.pdfconverter.service.PdfConversionService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PdfConversionServiceApplication {


    public static void main(String...args) throws IOException {
        PdfConversionService pdfConversionService = new PdfConversionService();
        pdfConversionService.run();
    }

}
