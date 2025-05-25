package ru.otus.java.basic.http.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRequest {
    private static final Logger logger = LogManager.getLogger(HttpRequest.class);
    private final String rawRequest;
    private HttpMethod method;
    private String body;
    private String uri;
    private Map<String, String> parameters;

    public HttpMethod getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public String getUri() {
        return uri;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public boolean containsParameter(String key) {
        return parameters.containsKey(key);
    }

    public HttpRequest(String rawRequest) throws MalformedURLException {
        this.rawRequest = rawRequest;
        this.parameters = new HashMap<>();
        this.parse();
    }

    private void parse() throws MalformedURLException {
        logger.info("Запуск парсинга HttpRequest. На вход получен raw request:{}{}", System.lineSeparator(), rawRequest);

        int startIndex = rawRequest.indexOf(' ');
        int endIndex = rawRequest.indexOf(' ', startIndex + 1);
        this.method = HttpMethod.valueOf(rawRequest.substring(0, startIndex));
        logger.debug("method: {}", this.method);

        this.uri = rawRequest.substring(startIndex + 1, endIndex);
        if (uri.contains("?")) {
            String[] elements = uri.split("[?]");
            uri = elements[0];
            String[] rawParams = elements[1].split("[&]");
            for (String param : rawParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length != 2) {
                    throw new MalformedURLException("Некорректно задан параметр запроса.");
                }
                parameters.put(keyValue[0], keyValue[1]);
            }
        }
        logger.debug("uri: {}", this.uri);

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            this.body = rawRequest.substring(rawRequest.indexOf("\r\n\r\n"));
            logger.debug("body: {}", this.body);
        }
    }

    public int contentLength() throws IOException {
        byte[] bytes = rawRequest.getBytes(StandardCharsets.UTF_8);
        if (bytes == null) {
            throw new IOException("Запрос клиента не может быть пустым.");
        }
        return bytes.length;
    }

    public void checkLength() throws IOException {
        if (this.contentLength() > Application.getHttpRequestSizeLimit()) {
            throw new IOException("Размер запроса клиента в " + this.contentLength() + " байт превышает установленный лимит в " + Application.getHttpRequestSizeLimit() + " байт.");
        }
    }

    public void info(boolean showRawRequest) throws IOException {
        logger.info("{}HTTP Request Info:", System.lineSeparator());
        if (showRawRequest) {
            logger.info("RAW REQUEST:\n{}", rawRequest);
        }
        logger.info("METHOD: {}", method);
        logger.info("URI: {}", uri);
        logger.info("PARAMETERS: {}", parameters);
        logger.info("BODY: {}",  body);
        logger.info("SIZE: {}", contentLength());
        logger.info("SIZE_LIMIT: {}", Application.getHttpRequestSizeLimit());
    }
}
