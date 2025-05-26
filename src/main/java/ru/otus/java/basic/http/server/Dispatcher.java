package ru.otus.java.basic.http.server;

import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProviderImpl;
import ru.otus.java.basic.http.server.exceptions.*;
import ru.otus.java.basic.http.server.processors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dispatcher {
    private static final Logger logger = LogManager.getLogger(Dispatcher.class);
    private final ItemsDatabaseProvider itemsDbProvider;
    private final Map<String, Map<String, RequestProcessor>> methods;
    private final RequestProcessor defaultStaticResourceProcessor;

    public Dispatcher() {
        this.itemsDbProvider = new ItemsDatabaseProviderImpl();
        this.methods = new HashMap<>();
        this.methods.put("/hello", Map.of("GET", new HelloProcessor()));
        this.methods.put("/calculator", Map.of("GET", new CalculatorProcessor()));
        this.methods.put("/items", Map.of(
                "GET", new GetItemProcessor(itemsDbProvider),
                "POST", new CreateItemProcessor(itemsDbProvider),
                "DELETE", new DeleteItemProcessor(itemsDbProvider),
                "PUT", new UpdateItemProcessor(itemsDbProvider)
        ));
        this.defaultStaticResourceProcessor = new DefaultStaticResourcesProcessor();
    }

    public void execute(HttpRequest request, OutputStream output) throws IOException {
        try {
            logger.info("Запуск диспетчера запросов.");
            if (Files.exists(Paths.get("static/", request.getUri().substring(1)))) {
                defaultStaticResourceProcessor.execute(request, output);
                return;
            }

            if (!methods.containsKey(request.getUri())) {
                throw new NotFoundException("404 PAGE NOT FOUND", "Запрошенный URI не найден на Web-сервере.");
            }

            Map<String, RequestProcessor> processors = methods.get(request.getUri());
            logger.info("processors: {}", processors);

            if (!processors.containsKey(request.getMethod().toString())) {
                if (request.getMethod().equals(HttpMethod.POST) && processors.containsKey("GET")) {
                    throw new MethodNotAllowed("405 METHOD NOT ALLOWED", "В запросе указан недопустимый HTTP-метод для запрошенного URI.");
                }
                throw new NotFoundException("404 PAGE NOT FOUND", "Запрошенный URI не найден на Web-сервере.");
            }

            processors.get(request.getMethod().toString()).execute(request, output);

        } catch (BadRequestException e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(request, output);
        } catch (NotFoundException e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(request, output);
        } catch (MethodNotAllowed e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(request, output);
        } catch (NotAcceptableResponse e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(request, output);
        } catch (Exception e) {
            new HttpErrorProcessor("500 INTERNAL SERVER ERROR", e.getMessage()).execute(request, output);
            /*
            String response = "" +
                    "HTTP/1.1 500 Internal Server Error\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    "<html><body><h1> INTERNAL SERVER ERROR : " + e.getMessage() + "</h1></body></html>";
            output.write(response.getBytes(StandardCharsets.UTF_8));
             */
        }
    }
}
