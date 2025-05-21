package ru.otus.java.basic.http.server.processors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpErrorProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(HttpErrorProcessor.class);
    private final String errorCode;
    private final String errorMessage;

    public HttpErrorProcessor(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) {
        logger.info("Для обработки ошибки {} запущен обработчик HTTP-запросов: {} ", errorCode, HttpErrorProcessor.class.getName());
        String errorCodePattern = "${errorCode}";
        String errorMessagePattern = "${errorMessage}";
        String filename= "error_" + errorCode.split(" ")[0] + ".html";
        try {
            Path filePath = Paths.get("static/", filename);
            byte[] fileBytes = Files.readAllBytes(filePath);
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            String modifiedContent = fileContent
                    .replace(errorCodePattern, errorCode)
                    .replace(errorMessagePattern, errorMessage);
            byte[] modifiedFileBytes = modifiedContent.getBytes(StandardCharsets.UTF_8);

            String response = "HTTP/1.1 " + errorCode + "\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + modifiedFileBytes.length + "\r\n" +
                    "Content-Disposition: inline\r\n" +
                    "\r\n";
            output.write(response.getBytes());
            output.write(modifiedFileBytes);
        } catch (IOException e) {
            logger.error("Возникла исключительная ситуация при выполнении обработчика HTTP-ошибок.");
            e.printStackTrace();
        }
    }
}
