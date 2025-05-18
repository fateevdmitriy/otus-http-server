package ru.otus.java.basic.http.server;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProviderImpl;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import ru.otus.java.basic.http.server.exceptions.ErrorDto;
import ru.otus.java.basic.http.server.processors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dispatcher {
    private static final Logger logger = LogManager.getLogger(Dispatcher.class);
    private static final String HTTP_500_ERR_MSG = "Server encountered an unexpected condition that prevented it from fulfilling the request.";
    private static final String HTTP_405_ERR_MSG = "URI can't be processed as POST request, but  can be processed as GET request.";
    private final ItemsDatabaseProvider itemsDbProvider;
    private final Map<String, Map<String, RequestProcessor>> processors;
    private final RequestProcessor defaultNotFoundProcessor;
    private final RequestProcessor defaultStaticResourceProcessor;

    public Dispatcher() {
        this.itemsDbProvider = new ItemsDatabaseProviderImpl();
        this.processors = new HashMap<>();
        this.processors.put("/hello", Map.of("GET", new HelloProcessor()));
        this.processors.put("/calculator", Map.of("GET", new CalculatorProcessor()));
        this.processors.put("/items", Map.of(
                "GET", new CalculatorProcessor(),
                "POST", new CreateItemProcessor(itemsDbProvider),
                "DELETE", new DeleteItemProcessor(itemsDbProvider),
                "PUT", new UpdateItemProcessor(itemsDbProvider)
        ));
        this.defaultNotFoundProcessor = new DefaultNotFoundProcessor();
        this.defaultStaticResourceProcessor = new DefaultStaticResourcesProcessor();
    }

    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запуск диспетчера запросов.");
        if (Files.exists(Paths.get("static/", request.getUri().substring(1)))) {
            defaultStaticResourceProcessor.execute(request, output);
            return;
        }
        if (!processors.containsKey(request.getUri())) {
            defaultNotFoundProcessor.execute(request, output);
            return;
        }
        // TEST IT
        Map<String, RequestProcessor> methods = processors.get(request.getUri());
        logger.info("methods: {}", methods);

        if (!methods.containsKey(request.getMethod().toString())) {
            if (request.getMethod().equals(HttpMethod.POST) && methods.containsKey("GET")) {
                logger.info("Response 405!");
                String response = "" +
                        "HTTP/1.1 405 Method Not Allowed\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n" +
                        "<html><body><h1>" + HTTP_405_ERR_MSG + "</h1></body></html>";
                output.write(response.getBytes(StandardCharsets.UTF_8));
                return;
            }
            defaultNotFoundProcessor.execute(request, output);
            return;
        }

        try {
            methods.get(request.getMethod().toString()).execute(request, output);
        } catch (BadRequestException e) {
            Gson gson = new Gson();
            ErrorDto errorDto = new ErrorDto(e.getCode(), e.getMessage());
            String errorDtoJson = gson.toJson(errorDto);
            String response = "" +
                    "HTTP/1.1 400 Bad Request\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n" + errorDtoJson;
            output.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            String response = "" +
                    "HTTP/1.1 500 Internal Server Error\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    "<html><body><h1>" + HTTP_500_ERR_MSG + "</h1></body></html>";
            output.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
