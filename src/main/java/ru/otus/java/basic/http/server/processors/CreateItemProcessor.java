package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(CreateItemProcessor.class);
    private ItemsRepository itemsRepository;

    public CreateItemProcessor(ItemsRepository itemsRepository) {
        this.itemsRepository = itemsRepository;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", CreateItemProcessor.class.getName());
        Gson gson = new Gson();
        Item newItem = gson.fromJson(request.getBody(), Item.class);
        logger.debug("Название нового продукта: {}", newItem.getTitle());
        if (newItem.getTitle() == null || newItem.getTitle().isEmpty()) {
            throw new BadRequestException("INCORRECT_REQUEST_DATA", "В параметре запроса название продукта не может быть пустым.");
        }
        logger.debug("Цена нового продукта: {}", newItem.getPrice());
        if (newItem.getPrice() != null && newItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("INCORRECT_REQUEST_DATA", "В параметре запроса цена продукта не может быть отрицательной.");
        }
        itemsRepository.addNewItem(newItem);
        logger.debug("Хранилище товаров содержит: {}", itemsRepository.getAllItems());
        String response = "" +
                "HTTP/1.1 201 Created\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
