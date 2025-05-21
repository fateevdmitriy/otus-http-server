package ru.otus.java.basic.http.server.processors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
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

    //TODO Выводить errorMessage на html-странице ошибки
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Для обработки ошибки {} запущен обработчик HTTP-запросов: {} ", errorCode, HttpErrorProcessor.class.getName());
        String filename= "error_" + errorCode.split(" ")[0] + ".html";
        Path filePath = Paths.get("static/", filename);
        byte[] fileData = Files.readAllBytes(filePath);
        String response = "HTTP/1.1 " + errorCode + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + fileData.length + "\r\n" +
                "Content-Disposition: inline\r\n" +
                "\r\n";
        output.write(response.getBytes());
        output.write(fileData);
    }
}
