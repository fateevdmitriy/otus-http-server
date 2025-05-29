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
    private final Map<String, Map<String, RequestProcessor>> methods;
    private final RequestProcessor defaultStaticResourceProcessor;

    public Dispatcher() {
        ItemsDatabaseProvider itemsDbProvider = new ItemsDatabaseProviderImpl();
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
            logger.info("Запуск диспетчера запросов.");

            if (Files.exists(Paths.get("static/", request.getUri().substring(1)))) {
                defaultStaticResourceProcessor.execute(request, output);
                return;
            }

            if (!methods.containsKey(request.getUri())) {
                throw new NotFoundException("404 PAGE NOT FOUND", "Запрошенный URI не найден на Web-сервере.");
            }

            Map<String, RequestProcessor> processors = methods.get(request.getUri());

            if (!processors.containsKey(request.getMethod().toString())) {
                if (request.getMethod().equals(HttpMethod.POST) && processors.containsKey("GET")) {
                    throw new MethodNotAllowedException("405 METHOD NOT ALLOWED", "В запросе указан недопустимый HTTP-метод для запрошенного URI.");
                }
                throw new NotFoundException("404 PAGE NOT FOUND", "Запрошенный URI не найден на Web-сервере.");
            }

            processors.get(request.getMethod().toString()).execute(request, output);
    }
}
