# PDF Conversion Service Application

## Overview

The PDF Conversion Service Application is designed to process PDF files, extracting text using AWS Textract and overlaying the extracted text back onto the original PDF to create a searchable PDF document. The application can run in two modes: cloud mode (working with PDFs stored on AWS S3) and local mode (working with PDFs stored locally).

## Features

- **AWS S3 Integration**: Download and upload PDFs from and to an AWS S3 bucket.
- **AWS Textract Integration**: Extract text and associated metadata from PDFs using AWS Textract.
- **PDFBox Integration**: Overlay extracted text onto the original PDF to create a searchable PDF.
- **Spring Boot**: Built with Spring Boot for easy configuration and deployment.
- **Configurable**: Easy configuration using an `application.properties` file or environment variables.

## Prerequisites

- **Java 21**: Make sure you have JDK 21 installed.
- **Maven**: Apache Maven is required to build the project.
- **AWS Account**: AWS account with S3 and Textract services enabled.
- **Configuration**: An `application.properties` file or environment variables with the necessary AWS credentials and configuration settings.

## Installation

1. **Clone the repository:**

    ```bash
    git clone https://github.com/your-repo/pdf-conversion-service.git
    cd pdf-conversion-service
    ```

2. **Build the application:**

    ```bash
    mvn clean install
    ```

3. **Prepare configuration:**

   Create an `application.properties` file in the `resources` directory with the following content:

   ```properties
   aws.access.key=YOUR_AWS_ACCESS_KEY
   aws.secret.key=YOUR_AWS_SECRET_KEY
   aws.s3.bucket=YOUR_S3_BUCKET_NAME
   aws.s3.object=YOUR_S3_OBJECT_KEY
   aws.region=YOUR_AWS_REGION
   ```

   Alternatively, you can use environment variables with the [Java Dotenv](https://github.com/cdimascio/java-dotenv) library:

   ```bash
   export AWS_ACCESS_KEY=YOUR_AWS_ACCESS_KEY
   export AWS_SECRET_KEY=YOUR_AWS_SECRET_KEY
   export AWS_S3_BUCKET=YOUR_S3_BUCKET_NAME
   export AWS_S3_OBJECT=YOUR_S3_OBJECT_KEY
   export AWS_REGION=YOUR_AWS_REGION
   ```

## Usage

### Running the Application

The application can be run in two modes:

1. **Cloud Mode**: Processes PDFs stored on AWS S3.

    ```bash
    java -jar target/pdfconverter-1.0.0.jar cloud
    ```

2. **Local Mode**: Processes PDFs stored locally.

    ```bash
    java -jar target/pdfconverter-1.0.0.jar local
    ```

### Main Commands

- `cloud`: Downloads a PDF from the specified S3 bucket, processes it, and uploads the searchable PDF back to S3.
- `local`: Processes a local PDF file and saves the searchable PDF locally.

### Example Usage

- **To process a PDF in S3:**

    ```bash
    java -jar target/pdfconverter-1.0.0.jar cloud
    ```

- **To process a local PDF:**

    ```bash
    java -jar target/pdfconverter-1.0.0.jar local
    ```

## Project Structure

- **`PdfConversionServiceApplication.java`**: The entry point of the application. Handles command-line arguments to determine the mode (`cloud` or `local`).
- **`PdfConversionService.java`**: The core service that manages the processing of PDFs using AWS Textract and PDFBox.
- **`PDFTextExtractor.java`**: Responsible for extracting text from PDFs using AWS Textract.
- **`PdfCreatorFromAWS.java`**: Handles overlaying the extracted text onto the original PDF to create a searchable PDF.
- **`ConfigLoader.java`**: Loads configuration properties from the `application.properties` file or environment variables.

## Dependencies

The project uses the following dependencies:

- **Spring Boot Starter Web**: Provides a web server and RESTful API support.
- **AWS SDK for Java**:
    - `aws-java-sdk-textract`: To interact with AWS Textract.
    - `aws-java-sdk-s3`: To interact with AWS S3.
- **Apache PDFBox**: Used for PDF manipulation and text overlay.
- **Lombok**: Reduces boilerplate code by generating getters, setters, and constructors automatically.
- **Java Dotenv**: Loads environment variables from a `.env` file.

These dependencies are managed via Maven.

## Logging

The application uses SLF4J with Logback for logging. Logs provide detailed information on the processing steps and can help in debugging issues.

## Contributing

Feel free to fork this repository, make changes, and create pull requests. Contributions are always welcome!

