package ru.otus.java.basic.http.server.processors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.Application;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsDatabaseProvider;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponse;
import ru.otus.java.basic.http.server.exceptions.NotFoundException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class DeleteItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(DeleteItemProcessor.class);
    private static final String PROCESSOR_CONTENT_TYPE = "text/html";
    private final ItemsDatabaseProvider itemsDbProvider;

    public DeleteItemProcessor(ItemsDatabaseProvider itemsDbProvider) {
        this.itemsDbProvider = itemsDbProvider;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        logger.info("Запущен обработчик HTTP-запросов: {} ", DeleteItemProcessor.class.getName());
        if (!request.getHeaderAccept().equals("*/*") && !request.getHeaderAccept().contains(PROCESSOR_CONTENT_TYPE)) {
            throw new NotAcceptableResponse("406 NOT ACCEPTABLE", "Тип ответа сервера: "
                    + PROCESSOR_CONTENT_TYPE + ", клиент принимает типы: " + request.getHeaderAccept());
        }
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
        Map<String,String> responseHeaders = Map.of("Content-Type", PROCESSOR_CONTENT_TYPE);
        HttpResponse response = new HttpResponse(Application.getHttpVersion(), "204", "No Content", responseHeaders);
        response.info();
        response.checkLength();
        output.write(response.getBytes());
    }

}
