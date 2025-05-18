package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(UpdateItemProcessor.class);
    private final ItemsDatabaseProvider itemsDbProvider;

    public UpdateItemProcessor(ItemsDatabaseProvider itemsDbProvider) {
        this.itemsDbProvider = itemsDbProvider;
    }

    //TODO Добавить возможность обновления нескольких товаров
    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", UpdateItemProcessor.class.getName());
        Gson gson = new Gson();
        Item updItem = gson.fromJson(request.getBody(), Item.class);

        logger.info("Идентификатор обновляемого продукта: {}", updItem.getId());
        logger.info("Название обновляемого продукта: {}", updItem.getTitle());
        logger.info("Цена обновляемого продукта: {}", updItem.getPrice());
        logger.info("Вес обновляемого продукта: {}", updItem.getWeight());
        if (updItem.getId() == null) {
            throw new BadRequestException("INCORRECT_REQUEST_DATA", "В параметре запроса идентификатор продукта не может быть пустым.");
        }
        if (updItem.getTitle() == null || updItem.getTitle().isEmpty()) {
            throw new BadRequestException("INCORRECT_REQUEST_DATA", "В параметре запроса название продукта не может быть пустым.");
        }
        if (updItem.getPrice() != null && updItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("INCORRECT_REQUEST_DATA", "В параметре запроса цена продукта не может быть отрицательной.");
        }
        if (itemsDbProvider.getItemById(updItem.getId()) == null) {
            String response = "" +
                    "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    "RESOURCE NOT FOUND";
            output.write(response.getBytes(StandardCharsets.UTF_8));
            return;
        }
        int itemsUpdatedCnt = itemsDbProvider.updateItem(updItem);
        logger.info("Обновлено товаров: {}", itemsUpdatedCnt);
        String response = "" +
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }

}



