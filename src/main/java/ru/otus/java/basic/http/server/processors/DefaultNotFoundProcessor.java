package ru.otus.java.basic.http.server.processors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DefaultNotFoundProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(DefaultNotFoundProcessor.class);

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        String response = "" +
                "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body><h1>PAGE NOT FOUND</h1></body></html>";
        logger.info("Запущен обработчик HTTP-запросов: {} ", DefaultNotFoundProcessor.class.getName());
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
