package ru.otus.java.basic.http.server.processors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.HttpResponse;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class HttpErrorProcessor implements RequestProcessor {
    private static final Logger logger = LogManager.getLogger(HttpErrorProcessor.class);
    private static final String PROCESSOR_CONTENT_TYPE = "text/html";
    private final String errorCode;
    private final String errorMessage;

    public HttpErrorProcessor(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) {
        logger.info("Для обработки ошибки {} запущен обработчик HTTP-запросов: {} ", errorCode, HttpErrorProcessor.class.getName());
        if (!request.getHeaderAccept().equals("*/*") && !request.getHeaderAccept().contains(PROCESSOR_CONTENT_TYPE)) {
            throw new NotAcceptableResponse("406 NOT ACCEPTABLE","Сервер не может вернуть ответ типа, который приемлем клиентом.");
        }
        String errorCodePattern = "${errorCode}";
        String errorMessagePattern = "${errorMessage}";
        String[] elements = errorCode.split(" ",2);
        String filename= "error_" + elements[0] + ".html";
        File usersFile = new File(filename);
        try {
            Path filePath = Paths.get("static/", filename);
            byte[] fileBytes = Files.readAllBytes(filePath);
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            String modifiedContent = fileContent
                    .replace(errorCodePattern, errorCode)
                    .replace(errorMessagePattern, errorMessage);
            byte[] modifiedFileBytes = modifiedContent.getBytes(StandardCharsets.UTF_8);
            Map<String,String> responseHeaders = Map.of("Content-Type", PROCESSOR_CONTENT_TYPE,
                                                       "Content-Length", String.valueOf(modifiedFileBytes.length),
                                                       "Content-Disposition", "inline"
                                                       );
            HttpResponse response = new HttpResponse("HTTP/1.1", elements[0], elements[1], responseHeaders, modifiedFileBytes);
            response.info();
            response.checkLength();
            output.write(response.getBytes());
        } catch (IOException e) {
            logger.error("Возникла исключительная ситуация при выполнении обработчика HTTP-ошибок.");
            e.printStackTrace();
        }
    }
}
