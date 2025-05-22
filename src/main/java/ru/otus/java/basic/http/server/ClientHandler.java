package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final int BUFFER_SIZE = 256;
    private final Socket clientSocket;
    private final Dispatcher dispatcher;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.dispatcher = new Dispatcher();
    }

    @Override
    public void run() {
        logger.info("Новый клиент подключился к серверу.");
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = clientSocket.getInputStream();
            if (inputStream != null) {
                BufferedInputStream bufInStream = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = -1;
                while ((bytesRead = bufInStream.read(buffer)) != -1) {
                    logger.info("bytesRead: {}", bytesRead);
                    if (bytesRead > 0) {
                        stringBuilder.append(new String(buffer, 0, bytesRead));
                    }
                    if (bytesRead < BUFFER_SIZE) {
                        break;
                    }
                }
            }
            String rawRequest = stringBuilder.toString();
            logger.info("-> rawRequest:{}{}", System.lineSeparator(), rawRequest);
            if (rawRequest.isEmpty()) {
                throw new IOException("Получен пустой запрос от клиента.");
            }

            HttpRequest request = new HttpRequest(rawRequest);
            request.info(true);
            if (request.isSizeLimitExceeded()) {
                throw new IOException("Размер клиентского запроса в " +request.contentLength() + " байт превышает установленный лимит в " + HttpRequest.REQUEST_SIZE_LIMIT + " байт.");
            }
            dispatcher.execute(request, clientSocket.getOutputStream());

        } catch (IOException e) {
            logger.error("Возникло исключение при соединении клиента с сервером. {}", e.getMessage());
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.error("Возникло исключение при завершении соединения клиента с сервером.");
            e.printStackTrace();
        }
    }
}
