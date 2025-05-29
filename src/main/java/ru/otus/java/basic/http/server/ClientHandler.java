package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import ru.otus.java.basic.http.server.exceptions.MethodNotAllowedException;
import ru.otus.java.basic.http.server.exceptions.NotAcceptableResponseException;
import ru.otus.java.basic.http.server.exceptions.NotFoundException;
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
        try {
            String rawRequest = readRawRequestFromClient(clientSocket);
            logger.debug("rawRequest:{}{}", System.lineSeparator(), rawRequest);
            if (rawRequest.isEmpty()) {
                logger.error("Получен пустой запрос от клиента.");
                return;
            }

            HttpRequest request = new HttpRequest(rawRequest);
            request.info(true);
            request.checkLength();
            dispatcher.execute(request, out);

        } catch (BadRequestException e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(out);
        } catch (NotFoundException e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(out);
        } catch (MethodNotAllowedException e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(out);
        } catch (NotAcceptableResponseException e) {
            new HttpErrorProcessor(e.getCode(), e.getMessage()).execute(out);
        } catch (IOException e) {
            new HttpErrorProcessor("503 SERVICE UNAVAILABLE", "Возникло исключение при соединении клиента с сервером." + e.getMessage()).execute(out);
        } catch (Exception e) {
            new HttpErrorProcessor("500 INTERNAL SERVER ERROR", e.getMessage()).execute(out);
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
        }
    }

    private String readRawRequestFromClient(Socket clientSocket) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        InputStream inputStream = clientSocket.getInputStream();
        logger.info("Новый клиент подключился к серверу.");
        if (inputStream != null) {
            BufferedInputStream bufInStream = new BufferedInputStream(inputStream);
            int bufferSize = Application.getClientHandlerBufferSize();
            byte[] buffer = new byte[bufferSize];
            int bytesRead = -1;
            while ((bytesRead = bufInStream.read(buffer)) != -1) {
                if (bytesRead > 0) {
                    stringBuffer.append(new String(buffer, 0, bytesRead));
                }
                if (bytesRead < bufferSize) {
                    break;
                }
            }
        } else {
            throw new IOException("Не удалось получить соединение клиента с сервером.");
        }
        return stringBuffer.toString();
    }
}
