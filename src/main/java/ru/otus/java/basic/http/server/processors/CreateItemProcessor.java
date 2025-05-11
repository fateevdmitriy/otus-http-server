package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
        Gson gson = new Gson();
        Item newItem = gson.fromJson(request.getBody(), Item.class);
        itemsRepository.addNewItem(newItem);
        String response = "" +
                "HTTP/1.1 201 Created\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n";
        
        logger.info("Запущен обработчик HTTP-запросов: {} ", CreateItemProcessor.class.getName());    
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
