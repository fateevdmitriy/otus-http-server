package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Map;

import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponse;

public class CreateItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(CreateItemProcessor.class);
    private static final String PROCESSOR_CONTENT_TYPE = "text/html";
    private final ItemsDatabaseProvider itemsDbProvider;

    public CreateItemProcessor(ItemsDatabaseProvider itemsDbProvider) {
        this.itemsDbProvider = itemsDbProvider;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", CreateItemProcessor.class.getName());
        if (!request.getHeaderAccept().equals("*/*") && !request.getHeaderAccept().contains(PROCESSOR_CONTENT_TYPE)) {
            throw new NotAcceptableResponse("406 NOT ACCEPTABLE","Сервер не может вернуть ответ типа, который приемлем клиентом.");
        }
        Gson gson = new Gson();
        Item newItem = gson.fromJson(request.getBody().toString(), Item.class);
        logger.info("Название нового продукта: {}", newItem.getTitle());
        logger.info("Цена нового продукта: {}", newItem.getPrice());
        logger.info("Вес нового продукта: {}", newItem.getWeight());
        if (newItem.getTitle() == null || newItem.getTitle().isEmpty()) {
            throw new BadRequestException("400 BAD REQUEST", "В параметре запроса название продукта не может быть пустым.");
        }
        if (newItem.getPrice() != null && newItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("400 BAD REQUEST", "В параметре запроса цена продукта не может быть отрицательной.");
        }
        int itemsAddedCnt = itemsDbProvider.addItem(newItem);
        logger.info("Добавлено товаров: {}",itemsAddedCnt);
        Map<String,String> responseHeaders = Map.of("Content-Type", PROCESSOR_CONTENT_TYPE);
        HttpResponse response = new HttpResponse("HTTP/1.1", "201", "Created", responseHeaders);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }
}
