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
    private final ItemsDatabaseProvider itemsDbProvider;
    private final Map<String, RequestProcessor> processors;
    private final RequestProcessor defaultNotFoundProcessor;
    private final RequestProcessor defaultStaticResourceProcessor;

    public Dispatcher() {
        this.itemsDbProvider = new ItemsDatabaseProviderImpl();
        this.processors = new HashMap<>();
        this.processors.put("GET /hello", new HelloProcessor());
        this.processors.put("GET /calculator", new CalculatorProcessor());
        this.processors.put("GET /items", new GetItemProcessor(itemsDbProvider));
        this.processors.put("POST /items", new CreateItemProcessor(itemsDbProvider));
        this.processors.put("DELETE /items", new DeleteItemProcessor(itemsDbProvider));
        this.processors.put("PUT /items", new UpdateItemProcessor(itemsDbProvider));
        this.defaultNotFoundProcessor = new DefaultNotFoundProcessor();
        this.defaultStaticResourceProcessor = new DefaultStaticResourcesProcessor();
    }

    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запуск диспетчера запросов.");
        if (Files.exists(Paths.get("static/", request.getUri().substring(1)))) {
            defaultStaticResourceProcessor.execute(request, output);
            return;
        }
        if (!processors.containsKey(request.getRoutingKey())) {
            defaultNotFoundProcessor.execute(request, output);
            return;
        }
        try {
            processors.get(request.getRoutingKey()).execute(request, output);
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
