package com.example.pdfconverter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class PdfConversionServiceApplication {


    public static void main(String... args) {
        SpringApplication.run(PdfConversionServiceApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            log.info("message from application.properties " + environment.getProperty("message-from-application-properties"));
        };
    }

}
