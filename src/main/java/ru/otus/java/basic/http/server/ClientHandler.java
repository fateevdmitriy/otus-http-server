package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.processors.HttpErrorProcessor;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Dispatcher dispatcher;
    private final OutputStream out;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.dispatcher = new Dispatcher();
        this.out = clientSocket.getOutputStream();
    }

    @Override
    public void run() {
        logger.info("Новый клиент подключился к серверу.");

        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = clientSocket.getInputStream();
            if (inputStream != null) {
                BufferedInputStream bufInStream = new BufferedInputStream(inputStream);
                int bufferSize = Application.getClientHandlerBufferSize();
                byte[] buffer = new byte[bufferSize];
                int bytesRead = -1;
                while ((bytesRead = bufInStream.read(buffer)) != -1) {
                    if (bytesRead > 0) {
                        stringBuilder.append(new String(buffer, 0, bytesRead));
                    }
                    if (bytesRead < bufferSize) {
                        break;
                    }
                }
            }
            String rawRequest = stringBuilder.toString();
            logger.info("rawRequest:{}{}", System.lineSeparator(), rawRequest);
            if (rawRequest.isEmpty()) {
                logger.error("Получен пустой запрос от клиента.");
                return;
            }

            HttpRequest request = new HttpRequest(rawRequest);
            request.info(true);
            request.checkLength();
            dispatcher.execute(request, out);

        } catch (IOException e) {
            new HttpErrorProcessor("503 SERVICE UNAVAILABLE", "Возникло исключение при соединении клиента с сервером.").execute(null, out);
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
