package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private final int port;
    private static final Logger logger = LogManager.getLogger(Application.class);     
    
    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Сервер запущен на порту: " + port);
            logger.info("[Info] Сервер запущен на порту: {}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();                 
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
