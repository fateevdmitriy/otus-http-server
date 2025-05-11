package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetItemProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(GetItemProcessor.class);
    private ItemsRepository itemsRepository;

    public GetItemProcessor(ItemsRepository itemsRepository) {
        this.itemsRepository = itemsRepository;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        if (request.getParameter("id") != null) {
            Long id = Long.parseLong(request.getParameter("id"));
            Item item = itemsRepository.findById(id);
            if (item == null) {
                String response = "" +
                        "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n" +
                        "RESOURCE NOT FOUND";

                logger.info("Запущен обработчик HTTP-запросов: {}. Возвращает ошибку т.к. товар с заданным id не найден.",
                        GetItemProcessor.class.getName());
                output.write(response.getBytes(StandardCharsets.UTF_8));
                return;
            }
            Gson gson = new Gson();
            String itemResponse = gson.toJson(item);
            String response = "" +
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n" +
                    itemResponse;
            
            logger.info("Запущен обработчик HTTP-запросов: {}. Возвращает товар с заданным id.", GetItemProcessor.class.getName());    
            output.write(response.getBytes(StandardCharsets.UTF_8));
            return;
        }
        List<Item> items = itemsRepository.getAllItems();
        Gson gson = new Gson();
        String itemsResponse = gson.toJson(items);
        String response = "" +
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n" +
                itemsResponse;
        
        logger.info("Запущен обработчик HTTP-запросов: {}. Возвращает перечень товаров.", GetItemProcessor.class.getName());    
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
