package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final int BUFFER_SIZE = 8192;  
    private final Socket clientSocket;
    private final Dispatcher dispatcher;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.dispatcher = new Dispatcher();
    }

    @Override
    public void run() {
        try {
            logger.info("Новый клиент подключился к серверу.");
            byte[] buffer = new byte[BUFFER_SIZE];
            int n = clientSocket.getInputStream().read(buffer);
            if (n == 0) {
                logger.error("Получено пустое сообщение от клиента.");
                throw new IOException("Получено пустое сообщение от клиента.");
            }
            if (n < 0) {
                logger.error("Получено битое сообщение от клиента.");
                throw new IOException("Получено битое сообщение от клиента.");
            } 
            String rawRequest = new String(buffer, 0, n);
            if (rawRequest.isEmpty()) {
                logger.error("Получен пустой запрос от клиента.");
                throw new IOException("Получен пустой запрос от клиента.");
            }
            HttpRequest request = new HttpRequest(rawRequest);
            request.info(true);
            dispatcher.execute(request, clientSocket.getOutputStream());
        } catch (IOException e) {
            logger.error("Возникла исключительная ситуация при выполнении соединения клиента с сервером.");
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            if (clientSocket.getInputStream() != null) {
                clientSocket.getInputStream().close();
            }
            if (clientSocket.getOutputStream() != null) {
                clientSocket.getOutputStream().close();
            }
            clientSocket.close();             
        } catch (IOException e) {
            logger.error("Возникла исключительная ситуация при завершении соединения клиента с сервером.");
            e.printStackTrace();
        }
    }
}
