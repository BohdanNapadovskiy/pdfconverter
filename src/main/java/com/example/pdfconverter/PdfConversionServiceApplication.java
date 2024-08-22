package com.example.pdfconverter;

import com.example.pdfconverter.service.PdfConversionService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PdfConversionServiceApplication {


    public static void main(String...args) throws IOException {
        PdfConversionService pdfConversionService = new PdfConversionService();
        if (args.length == 0) {
            log.error("Please provide a command: 'cloud' or 'local'");
            return;
        }
        String command = args[0];
        switch (command) {
            case "cloud":
                pdfConversionService.run();
                break;
            case "local":
                pdfConversionService.runLocal();
                break;
            default:
                System.out.println("Unknown command. Please use 'cloud' or 'local'.");
                break;
        }
    }

}
