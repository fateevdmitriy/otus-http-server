package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpResponse {
    private static final Logger logger = LogManager.getLogger(HttpResponse.class);

    private String protocol;
    private String statusCode;
    private String statusText;
    private List<String> headers;
    private String textBody;
    private byte[] fileBody;
    private byte[] bytes;

    public String getProtocol() {
        return protocol;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public HttpResponse(String protocol,
                        String statusCode,
                        String statusText,
                        List<String> headers
                        ) {
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.bytes = this.compose();
    }

    public HttpResponse(String protocol,
                        String statusCode,
                        String statusText,
                        List<String> headers,
                        String textBody
                        ) {
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.textBody = textBody;
        this.bytes = this.compose();
    }

    public HttpResponse(String protocol,
                        String statusCode,
                        String statusText,
                        List<String> headers,
                        byte[] fileBody
                        ) {
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.fileBody = fileBody;
        this.bytes = this.compose();
    }

    public byte[] compose() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(this.getProtocol())
                        .append(" ")
                        .append(this.getStatusCode())
                        .append(" ")
                        .append(this.getStatusText())
                        .append(System.lineSeparator());
        if (!headers.isEmpty()) {
            for (String header : headers) {
                responseBuilder.append(header)
                               .append(System.lineSeparator());
            }
        }
        responseBuilder.append(System.lineSeparator());

        byte[] result = new byte[0];
        if (textBody != null && !textBody.isEmpty()) {
            responseBuilder.append(textBody);
            logger.info("Response with textBody:{}{}", System.lineSeparator(), responseBuilder);
            result = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
        } else if (fileBody !=null && fileBody.length > 0) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(responseBuilder.toString().getBytes(StandardCharsets.UTF_8));
                baos.write(fileBody);
                logger.info("Response with fileBody:{}{}", System.lineSeparator(), baos.toString(StandardCharsets.UTF_8));
                result = baos.toByteArray();
            } catch (IOException e) {
                logger.info("Возникло исключение при формировании отклика сервером. {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            logger.info("Response without body:{}{}", System.lineSeparator(), responseBuilder);
            result = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }
        return result;
    }

    public int contentLength() throws IOException {
        if (this.getBytes() == null) {
            throw new IOException("Ответ сервера не может быть пустым.");
        }
        return this.getBytes().length;
    }

    public void checkLength() throws IOException {
        if (this.contentLength() > Application.getHttpResponseSizeLimit()) {
            throw new IOException("Размер ответа сервера в " + this.contentLength() + " байт превышает установленный лимит в " + Application.getHttpResponseSizeLimit() + " байт.");
        }
    }
    public void info() throws IOException {
        logger.info("{}HTTP Response Info:", System.lineSeparator());
        logger.info("PROTOCOL: {}", protocol);
        logger.info("STATUS CODE: {}", statusCode);
        logger.info("STATUS TEXT: {}", statusText);
        logger.info("HEADERS: {}",  headers);
        logger.info("TEXT BODY: {}",  textBody);
        logger.info("FILE BODY: {}",  fileBody);
        logger.info("SIZE: {}", contentLength());
        logger.info("SIZE_LIMIT: {}", Application.getHttpResponseSizeLimit());
    }
}
