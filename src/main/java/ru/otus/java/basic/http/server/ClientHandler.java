package ru.otus.java.basic.http.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Dispatcher dispatcher;
    private final BufferedReader in;
    private final BufferedWriter out;

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
            String rawRequest = in.readLine();
            HttpRequest request = new HttpRequest(rawRequest);
            request.info(true);
            dispatcher.execute(request, clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
