package ru.otus.java.basic.http.server;

public enum HttpMethod {
    GET, POST, PUT, DELETE;

    public static boolean contains(String checkNane)
    {
        for(HttpMethod method: values())
            if (method.name().equals(checkNane)) {
                return true;
            }
        return false;
    }
}
