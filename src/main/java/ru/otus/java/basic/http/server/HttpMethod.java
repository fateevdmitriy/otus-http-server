package ru.otus.java.basic.http.server;

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String name;

    HttpMethod(String name) {
        this.name = name;
    }

    public static boolean contains (String strName) {
        for (HttpMethod method : HttpMethod.values()) {
            if (method.name.equalsIgnoreCase(strName)) {
                return true;
            }
        }
        return false;
    }
}
