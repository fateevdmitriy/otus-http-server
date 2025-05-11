package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Dispatcher dispatcher;
    private final BufferedReader in;
    private final BufferedWriter out;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.dispatcher = new Dispatcher();
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        this.out =  new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            System.out.println("Новый клиент подключился к серверу.");
            logger.info("[Info] Новый клиент подключился к серверу.");
            String rawRequest = in.readLine();
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
            logger.info("[Info] Завершение соединения клиента с сервером.");
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
                }
        } catch (IOException e) {
            logger.error("Возникла исключительная ситуация при завершении соединения клиента с сервером.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
