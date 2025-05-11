package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultStaticResourcesProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(DefaultStaticResourcesProcessor.class);
    
    @Override
    public void execute(HttpRequest httpRequest, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", DefaultNotFoundProcessor.class.getName());
        String filename = httpRequest.getUri().substring(1);
        Path filePath = Paths.get("static/", filename);
        String fileType = filename.substring(filename.lastIndexOf(".") + 1);
        byte[] fileData = Files.readAllBytes(filePath);
        String contentDisposition = "";
        if (fileType.equals("txt")) {
            contentDisposition = "Content-Disposition: attachment;filename=" + filename + "\r\n";
        }
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + fileData.length + "\r\n" +
                contentDisposition +
                "\r\n";
        output.write(response.getBytes());
        output.write(fileData);
    }
}

