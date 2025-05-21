package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CalculatorProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(CalculatorProcessor.class);
    
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", CalculatorProcessor.class.getName());
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
        String response = "" +
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body><h1>" + a + " + " + b + " = " + (a + b) + "</h1></body></html>";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
