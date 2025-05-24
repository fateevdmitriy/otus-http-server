package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpResponse {
    private static final Logger logger = LogManager.getLogger(HttpResponse.class);

    private HttpRequest request;
    private String protocol;
    private String statusCode;
    private String statusText;
    private List<String> headers;
    private String textBody;
    private byte[] fileBody;

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public void setFileBody(byte[] fileBody) {
        this.fileBody = fileBody;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getTextBody() {
        return textBody;
    }

    public byte[] getFileBody() {
        return fileBody;
    }

    public HttpResponse(HttpRequest request, String protocol, String statusCode, String statusText, List<String> headers) {
        this.request = request;
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
    }

    public byte[] compose() {
        byte[] result = new byte[0];
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(this.getProtocol())
                        .append(" ")
                        .append(this.getStatusCode())
                        .append(" ")
                        .append(this.getStatusText())
                        .append("\r\n");
                        //.append(System.lineSeparator())

        if (!headers.isEmpty()) {
            for (String header : headers) {
                responseBuilder.append(header).append("\r\n");
            }
        }

        if (!textBody.isEmpty()) {
            responseBuilder.append("\r\n").append(textBody);
            logger.debug("Response with textBody:{}{}", System.lineSeparator(), responseBuilder);
            result = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
        } else if (fileBody.length > 0) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(responseBuilder.toString().getBytes(StandardCharsets.UTF_8));
                baos.write(fileBody);
                logger.debug("Response with fileBody:{}{}", System.lineSeparator(), baos.toString(StandardCharsets.UTF_8));
                result = baos.toByteArray();
            } catch (IOException e) {
                logger.error("Возникло исключение при формировании отклика сервером. {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            logger.debug("Response w/o body:{}{}", System.lineSeparator(), responseBuilder);
            result = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }
        return result;
    }

}
