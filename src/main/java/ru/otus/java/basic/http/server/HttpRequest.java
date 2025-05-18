package ru.otus.java.basic.http.server;

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

    public String getRoutingKey() {
        return method + " " + uri;
    }

    public boolean containsParameter(String key) {
        return parameters.containsKey(key);
    }

    public HttpRequest(String rawRequest) {
        this.rawRequest = rawRequest;
        this.parameters = new HashMap<>();
        this.parse();
    }

    private void parse() {
        logger.info("Запуск парсинга HttpRequest. На вход получен raw request: {}", rawRequest);
        int startIndex = rawRequest.indexOf(' ');
        int endIndex = rawRequest.indexOf(' ', startIndex + 1);
        logger.debug("startIndex: {}, endIndex: {}", startIndex, endIndex);
        this.method = HttpMethod.valueOf(rawRequest.substring(0, startIndex));
        logger.debug("method: {}", this.method);

        this.uri = rawRequest.substring(startIndex + 1, endIndex);
        if (uri.contains("?")) {
            String[] elements = uri.split("[?]");
            uri = elements[0];
            String[] rawParams = elements[1].split("[&]");
            for (String param : rawParams) {
                String[] keyValue = param.split("=");
                parameters.put(keyValue[0], keyValue[1]);
            }
        }
        logger.debug("uri: {}", this.uri);

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            this.body = rawRequest.substring(rawRequest.indexOf("\r\n\r\n"));
            logger.debug("body: {}", this.body);
        }
    }

    public void info(boolean showRawRequest) {
        if (showRawRequest) {
            logger.info("RAW REQUEST:\n{}", rawRequest);
        }
        logger.info("METHOD: {}", method);
        logger.info("URI: {}", uri);
        logger.info("PARAMETERS: {}", parameters);
        logger.info("BODY: {}",  body);
    }
}
