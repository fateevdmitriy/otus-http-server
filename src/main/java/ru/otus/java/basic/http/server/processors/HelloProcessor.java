package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.Application;
import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponseException;


public class HelloProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(HelloProcessor.class);
    private static final String PROCESSOR_CONTENT_TYPE = "text/html";

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", HelloProcessor.class.getName());

        if (!request.getHeaderAccept().equals("*/*") && !request.getHeaderAccept().toLowerCase().contains(PROCESSOR_CONTENT_TYPE.toLowerCase())) {
            throw new NotAcceptableResponseException("406 NOT ACCEPTABLE", "Тип ответа сервера: "
                    + PROCESSOR_CONTENT_TYPE + ", клиент принимает типы: " + request.getHeaderAccept());
        }

        final String HTML_BODY_HELLO = "<html><body><h1>Hello, USER!!!</h1></body></html>";
        Map<String,String> responseHeaders = Map.of("Content-Type", PROCESSOR_CONTENT_TYPE);
        HttpResponse response = new HttpResponse(Application.getHttpVersion(), "200", "OK", responseHeaders, HTML_BODY_HELLO);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }
}
