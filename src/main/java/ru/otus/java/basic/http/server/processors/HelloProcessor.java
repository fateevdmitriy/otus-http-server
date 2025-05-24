package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpResponse;

public class HelloProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(HelloProcessor.class);

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", HelloProcessor.class.getName());
        final String HTML_BODY_HELLO = "<html><body><h1>Hello, USER!!!</h1></body></html>";
        List<String> responseHeaders = List.of("Content-Type: text/html");
        HttpResponse response = new HttpResponse("HTTP/1.1", "200", "OK", responseHeaders, HTML_BODY_HELLO);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }
}
