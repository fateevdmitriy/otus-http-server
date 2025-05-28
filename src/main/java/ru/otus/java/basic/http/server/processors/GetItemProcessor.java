package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.Application;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponse;
import ru.otus.java.basic.http.server.exceptions.NotFoundException;

public class GetItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(GetItemProcessor.class);
    private static final String PROCESSOR_CONTENT_TYPE = "application/json";
    private final ItemsDatabaseProvider itemsDbProvider;

    public GetItemProcessor(ItemsDatabaseProvider itemsDbProvider) {
        this.itemsDbProvider = itemsDbProvider;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {}", GetItemProcessor.class.getName());

        logger.debug("request.getHeaderAccept(): {}", request.getHeaderAccept());
        if (!request.getHeaderAccept().equals("*/*") && !request.getHeaderAccept().contains(PROCESSOR_CONTENT_TYPE)) {
            throw new NotAcceptableResponse("406 NOT ACCEPTABLE", "Тип ответа сервера: "
                    + PROCESSOR_CONTENT_TYPE + ", клиент принимает типы: " + request.getHeaderAccept());
        }

        if (request.getParameter("id") != null) {
            Long id = Long.parseLong(request.getParameter("id"));
            Item item = itemsDbProvider.getItemById(id);
            if (item == null) {
                throw new NotFoundException("404 PAGE NOT FOUND", "Запрошенный URI не найден на Web-сервере.");
            }
            Gson gson = new Gson();
            String itemResponse = gson.toJson(item);
            Map<String,String> responseHeaders = Map.of("Content-Type", PROCESSOR_CONTENT_TYPE);
            HttpResponse response = new HttpResponse(Application.getHttpVersion(), "200", "OK", responseHeaders, itemResponse);
            response.info();
            response.checkLength();
            output.write(response.getBytes());
            return;
        }

        List<Item> items = itemsDbProvider.getAllItems();
        Gson gson = new Gson();
        String itemsResponse = gson.toJson(items);
        Map<String,String> responseHeaders = Map.of("Content-Type", PROCESSOR_CONTENT_TYPE);
        HttpResponse response = new HttpResponse(Application.getHttpVersion(), "200", "OK", responseHeaders, itemsResponse);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }
}
