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
    private final int[] version;
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

    public String getVersion() {
        return Integer.toString(version[0]) + "." + Integer.toString(version[1]);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeaderAccept() {
        if (headers.containsKey(HEADER_KEY_ACCEPT) && headers.get(HEADER_KEY_ACCEPT) != null) {
            return headers.get(HEADER_KEY_ACCEPT);
        }
        return "*/*";
    }

    public StringBuffer getBody() {
        return body;
    }

    public HttpRequest(String rawRequest) throws IOException {
        this.rawRequest = rawRequest;
        this.version = new int[2];
        this.parameters = new HashMap<>();
        this.headers = new HashMap<>();
        body = new StringBuffer();
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
        parseVersion(elements[2]);

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

    private void parseVersion(String rawVersion) throws MalformedURLException {
        if (rawVersion.indexOf("HTTP/") == 0 && rawVersion.indexOf('.') > 5) {
            String[] temp = rawVersion.substring(5).split("\\.");
            try {
                version[0] = Integer.parseInt(temp[0]);
                version[1] = Integer.parseInt(temp[1]);
            } catch (NumberFormatException e) {
                throw new MalformedURLException("В строке инициализации HTTP-запроса указана некорректная версия HTTP протокола: " + rawVersion);
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
        body.append(bodyLine.trim()).append(System.lineSeparator());
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
        logger.info("VERSION: {}", this.getVersion());
        logger.info("PARAMETERS: {}", parameters);
        logger.info("HEADERS: {}", headers);
        logger.info("BODY: {}",  body);
        logger.info("SIZE: {}", contentLength());
        logger.info("SIZE_LIMIT: {}", Application.getHttpRequestSizeLimit());
    }
}
