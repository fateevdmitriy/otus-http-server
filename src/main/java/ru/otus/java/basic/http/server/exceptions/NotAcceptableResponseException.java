package ru.otus.java.basic.http.server.exceptions;

public class NotAcceptableResponseException extends RuntimeException {
    private final String code;
    private final String message;

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public NotAcceptableResponseException(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

