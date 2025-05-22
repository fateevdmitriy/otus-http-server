package ru.otus.java.basic.http.server;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRequest {
    //public static final int REQUEST_SIZE_LIMIT = 1024000;
    public static final int REQUEST_SIZE_LIMIT = 10;
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

    public int contentLength() {
        if (this.body == null || this.body.isEmpty()) return 0;
        return this.body.getBytes(StandardCharsets.UTF_8).length;
    }

    public boolean isSizeLimitExceeded() {
        return this.contentLength() > REQUEST_SIZE_LIMIT;
    }

    public void info(boolean showRawRequest) {
        if (showRawRequest) {
            logger.info("RAW REQUEST:\n{}", rawRequest);
        }
        logger.info("METHOD: {}", method);
        logger.info("URI: {}", uri);
        logger.info("PARAMETERS: {}", parameters);
        logger.info("BODY: {}",  body);
        logger.info("SIZE: {}", contentLength());
    }
}
