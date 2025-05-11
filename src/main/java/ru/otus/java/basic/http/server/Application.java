package ru.otus.java.basic.http.server;

public class Application {
    private static final int SERVER_PORT = 8189;
    private static final int THREADS_NUM = 10;
    
    public static void main(String[] args) {
        new HttpServer(SERVER_PORT).start(THREADS_NUM);
    }
}
