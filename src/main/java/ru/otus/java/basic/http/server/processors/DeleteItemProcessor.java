package ru.otus.java.basic.http.server.processors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import ru.otus.java.basic.http.server.exceptions.NotFoundException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DeleteItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(DeleteItemProcessor.class);
    private final ItemsDatabaseProvider itemsDbProvider;

    public DeleteItemProcessor(ItemsDatabaseProvider itemsDbProvider) {
        this.itemsDbProvider = itemsDbProvider;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", DeleteItemProcessor.class.getName());
        if (request.getParameter("id") == null) {
            throw new BadRequestException("400 BAD REQUEST", "В параметре запроса не указан идентификатор удаляемого продукта.");
        }
        Long id = Long.parseLong(request.getParameter("id"));
        Item item = itemsDbProvider.getItemById(id);
        if (item == null) {
            throw new NotFoundException("404 PAGE NOT FOUND", "Запрошенный URI не найден на Web-сервере.");
        }
        int itemsDeletedCnt = itemsDbProvider.deleteItemById(id);
        logger.info("Удалено товаров: {}", itemsDeletedCnt);
        List<String> responseHeaders = List.of("Content-Type: text/html");
        HttpResponse response = new HttpResponse("HTTP/1.1", "204", "No Content", responseHeaders);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }

}
