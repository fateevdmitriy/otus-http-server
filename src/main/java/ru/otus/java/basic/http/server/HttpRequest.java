package ru.otus.java.basic.http.server;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String rawRequest;
    private HttpMethod method;
    private String body;
    private String uri;
    private Map<String, String> parameters;

    public HttpRequest(String rawRequest) {
        this.rawRequest = rawRequest;
        this.parameters = new HashMap<>();
        this.parse();
    }

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

    private void parse() {
        int startIndex = rawRequest.indexOf(' ');
        int endIndex = rawRequest.indexOf(' ', startIndex + 1);

        this.method = HttpMethod.valueOf(rawRequest.substring(0, startIndex));

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

        if (method == HttpMethod.POST) {
            this.body = rawRequest.substring(rawRequest.indexOf("\r\n\r\n"));
        }
    }

    public void info(boolean showRawRequest) {
        if (showRawRequest) {
            System.out.println("RAW REQUEST: " + rawRequest);
        }
        System.out.println("METHOD: " + method);
        System.out.println("URI: " + uri);
        System.out.println("PARAMETERS: " + parameters);
        System.out.println("BODY: " + body);
    }
}
