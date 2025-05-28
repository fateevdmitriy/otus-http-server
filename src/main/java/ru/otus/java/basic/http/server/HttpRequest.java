package ru.otus.java.basic.http.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;

public class HttpRequest {
    private static final Logger logger = LogManager.getLogger(HttpRequest.class);
    private static final String HEADER_KEY_ACCEPT = "Accept";

    private final String rawRequest;
    private HttpMethod method;
    private String uri;
    private final Map<String, String> parameters;
    private final Map<String, String> headers;
    private StringBuffer body;

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getParameters() { return parameters; }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public StringBuffer getBody() {
        return body;
    }

    public String getHeaderAccept() {
        if (headers.containsKey(HEADER_KEY_ACCEPT) && headers.get(HEADER_KEY_ACCEPT) != null) {
            return headers.get(HEADER_KEY_ACCEPT);
        }
        return "*/*";
    }

    public HttpRequest(String rawRequest) throws IOException {
        this.rawRequest = rawRequest;
        this.parameters = new HashMap<>();
        this.headers = new HashMap<>();
        this.parse();
    }

    public void parse() throws IOException {
        logger.info("Запуск парсинга HttpRequest. На вход получен raw request:{}{}", System.lineSeparator(), rawRequest);

        BufferedReader reader = new BufferedReader(new StringReader(rawRequest));
        String initial = reader.readLine();
        if (initial == null || initial.isEmpty() || Character.isWhitespace(initial.charAt(0))) {
            throw new MalformedURLException("Некорректно задана строка инициализации HTTP-запроса: " + initial);
        }
        String[] elements = initial.split("\\s");
        if (elements.length != 3) {
            throw new MalformedURLException("Некорректно задана строка инициализации HTTP-запроса: " + initial);
        }

        if (!HttpMethod.contains(elements[0].trim())) {
            throw new MalformedURLException("В строке инициализации HTTP-запроса указан неизвестный HTTP-метод: " + elements[0]);
        }
        method = HttpMethod.valueOf(elements[0]);

        parseUriAndParameters(elements[1]);

        // get headers
        String header = reader.readLine();
        while (header != null && !header.isEmpty()) {
            appendHeaderParameter(header);
            header = reader.readLine();
        }

        // get body
        String bodyLine = reader.readLine();
        while (bodyLine != null && !bodyLine.isEmpty()) {
            appendMessageBody(bodyLine);
            bodyLine = reader.readLine();
        }
    }

    private void parseUriAndParameters(String rawUri) throws MalformedURLException {
        if (rawUri.indexOf('?') < 0) {
            uri = rawUri;
        } else {
            String[] rawParams = rawUri.split("[?]");
            uri = rawParams[0];
            if (rawParams[1] != null && !rawParams[1].isEmpty()) {
                String[] params = rawParams[1].split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length != 2) {
                        throw new MalformedURLException("Некорректно задан параметр запроса.");
                    }
                    parameters.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }

    }

    private void appendHeaderParameter(String header)  {
        int idx = header.indexOf(":");
        if (idx == -1) {
            throw new BadRequestException("400 BAD REQUEST", "Некорректно задан заголовок HTTP-запроса: " + header);
        }
        headers.put(header.substring(0, idx).trim(), header.substring(idx + 1).trim());
    }

    private void appendMessageBody(String bodyLine) {
        body.append(bodyLine).append(System.lineSeparator());
    }

    public boolean containsParameter(String key) {
        return parameters.containsKey(key);
    }

    public int contentLength() {
        byte[] bytes = rawRequest.getBytes(StandardCharsets.UTF_8);
        return bytes.length;
    }

    public void checkLength() throws IOException {
        if (this.contentLength() > Application.getHttpRequestSizeLimit()) {
            throw new IOException("Размер запроса клиента в " + this.contentLength() + " байт превышает установленный лимит в " + Application.getHttpRequestSizeLimit() + " байт.");
        }
    }

    public void info(boolean showRawRequest) {
        logger.info("{}HTTP Request Info:", System.lineSeparator());
        if (showRawRequest) {
            logger.info("RAW REQUEST:\n{}", rawRequest);
        }
        logger.info("METHOD: {}", method);
        logger.info("URI: {}", uri);
        logger.info("PARAMETERS: {}", parameters);
        logger.info("HEADERS: {}", headers);
        logger.info("BODY: {}",  body);
        logger.info("SIZE: {}", contentLength());
        logger.info("SIZE_LIMIT: {}", Application.getHttpRequestSizeLimit());
    }
}
