package ru.otus.java.basic.http.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpServer {
    private final int port;
    private static final Logger logger = LogManager.getLogger(Application.class);     
    
    public HttpServer(int port) {
        this.port = port;
    }

    public void start(int numThreads) {
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен на порту: {}", port);
            serverSocket.setReuseAddress(true);
            while (true) {
                Socket clientSocket = serverSocket.accept();                 
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                executorService.execute(clientHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            logger.info("Сервер завершил работу на порту: {}", port);
        }
    }
}
