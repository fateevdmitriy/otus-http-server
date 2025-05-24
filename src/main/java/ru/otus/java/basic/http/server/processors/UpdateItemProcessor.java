package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.exceptions.NotFoundException;

public class UpdateItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(UpdateItemProcessor.class);
    private final ItemsDatabaseProvider itemsDbProvider;

    public UpdateItemProcessor(ItemsDatabaseProvider itemsDbProvider) {
        this.itemsDbProvider = itemsDbProvider;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", UpdateItemProcessor.class.getName());
        Gson gson = new Gson();
        Item updItem = gson.fromJson(request.getBody(), Item.class);
        updItem.info();
        if (updItem.getId() == null) {
            throw new BadRequestException("400 BAD REQUEST", "В параметре запроса идентификатор продукта не может быть пустым.");
        }
        if (updItem.getTitle() == null || updItem.getTitle().isEmpty()) {
            throw new BadRequestException("400 BAD REQUEST", "В параметре запроса название продукта не может быть пустым.");
        }
        if (updItem.getPrice() != null && updItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("400 BAD REQUEST", "В параметре запроса цена продукта не может быть отрицательной.");
        }
        if (itemsDbProvider.getItemById(updItem.getId()) == null) {
            throw new NotFoundException("404 PAGE NOT FOUND", "Запрошенный URI не найден на Web-сервере.");
        }
        int itemsUpdatedCnt = itemsDbProvider.updateItem(updItem);
        logger.info("Обновлено товаров: {}", itemsUpdatedCnt);
        List<String> responseHeaders = List.of("Content-Type: text/html");
        HttpResponse response = new HttpResponse("HTTP/1.1", "200", "OK", responseHeaders);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }

}
