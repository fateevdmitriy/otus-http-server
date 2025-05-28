package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponse;

public class CalculatorProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(CalculatorProcessor.class);
    private static final String PROCESSOR_CONTENT_TYPE = "text/html";

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", CalculatorProcessor.class.getName());
        logger.info("---> request.getHeaderAccept(): '{}'", request.getHeaderAccept());
        logger.info("---> request.getHeaderAccept().equals(*/*): {}", request.getHeaderAccept().equals("*/*"));
        if (!request.getHeaderAccept().equals("*/*") && !request.getHeaderAccept().contains(PROCESSOR_CONTENT_TYPE)) {
            throw new NotAcceptableResponse("406 NOT ACCEPTABLE","Сервер не может вернуть ответ типа, который приемлем клиентом.");
        }
        if (!request.containsParameter("a")) {
            throw new BadRequestException("400 BAD REQUEST", "Отсутствует параметр запроса 'a'");
        }
        if (!request.containsParameter("b")) {
            throw new BadRequestException("400 BAD REQUEST", "Отсутствует параметр запроса 'b'");
        }
        int a;
        try {
            a = Integer.parseInt(request.getParameter("a"));    
        } catch (NumberFormatException e) {
            throw new BadRequestException("400 BAD REQUEST", "Параметр запроса а не является целым числом");
        }
        int b;
        try {
            b = Integer.parseInt(request.getParameter("b"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("400 BAD REQUEST", "Параметр запроса b не является целым числом");
        }
        final String HTML_BODY_CALC = "<html><body><h1>" + a + " + " + b + " = " + (a + b) + "</h1></body></html>";
        Map<String,String> responseHeaders = Map.of("Content-Type", PROCESSOR_CONTENT_TYPE);
        HttpResponse response = new HttpResponse("HTTP/1.1", "200", "OK", responseHeaders, HTML_BODY_CALC);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }
}
