package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CalculatorProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(CalculatorProcessor.class);
    
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        int a = Integer.parseInt(request.getParameter("a"));
        int b = Integer.parseInt(request.getParameter("b"));
        
        String response = "" +
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body><h1>" + a + " + " + b + " = " + (a + b) + "</h1></body></html>";
        
        logger.info("Запущен обработчик HTTP-запросов: {} ", CalculatorProcessor.class.getName());        
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
