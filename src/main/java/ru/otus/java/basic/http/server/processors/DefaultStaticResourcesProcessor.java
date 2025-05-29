package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.Application;
import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponseException;

public class DefaultStaticResourcesProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(DefaultStaticResourcesProcessor.class);

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {}", DefaultStaticResourcesProcessor.class.getName());
        String filename = request.getUri().substring(1);
        Path filePath = Paths.get("static/", filename);
        String fileType = filename.substring(filename.lastIndexOf(".") + 1);
        Map<String, String> responseHeaders;
        switch (fileType) {
            case "html":
                responseHeaders = Map.of(
                        "Content-Disposition", "inline",
                        "Content-Type", "text/html"
                );
                break;
            case "txt":
                responseHeaders = Map.of(
                        "Content-Disposition", "attachment; filename=" + filename,
                        "Content-Type", "text/plain"
                );
                break;
            default:
                responseHeaders = Map.of(
                        "Content-Disposition", "attachment; filename=" + filename,
                        "Content-Type", "application/octet-stream"
                );
        }

        if (!request.getHeaderAccept().equals("*/*") && !request.getHeaderAccept().toLowerCase().contains(responseHeaders.get("Content-Type").toLowerCase())) {
            throw new NotAcceptableResponseException("406 NOT ACCEPTABLE", "Тип ответа сервера: "
                    + responseHeaders.get("Content-Type") + ", клиент принимает типы: " + request.getHeaderAccept());

        }
        byte[] fileData = Files.readAllBytes(filePath);
        HttpResponse response = new HttpResponse(Application.getHttpVersion(), "200", "OK", responseHeaders, fileData);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }
}

