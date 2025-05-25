package ru.otus.java.basic.http.server;

import java.util.Scanner;

public class Application {
    private static final int MIN_SERVER_PORT = 8185;
    private static final int MAX_SERVER_PORT = 8189;
    private static final int DEFAULT_SERVER_PORT = 8189;
    private static final int THREADS_NUM = 10;

    public static void main(String[] args) {
        int serverPort = requestValidServerPost();
        new HttpServer(serverPort).start(THREADS_NUM);
    }

    private static int requestValidServerPost() {
        final String DEFAULT_PORT_MSG = "Будет использован номер порта по умолчанию: " + DEFAULT_SERVER_PORT;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите номер порта на котором будет работать запускаемый HTTP-сервер." +
                System.lineSeparator() + "Номер порта должен быть в диапазоне " + MIN_SERVER_PORT + "-" + MAX_SERVER_PORT +":");
        String userInputStr = scanner.nextLine();
        if (userInputStr.isEmpty()) {
            System.out.println("Не задан номер порта сервера. " + DEFAULT_PORT_MSG);
            return DEFAULT_SERVER_PORT;
        }
        int userInputInt;
        try {
            userInputInt = Integer.parseInt (userInputStr.trim());
        } catch (NumberFormatException e) {
            System.out.println("Введённый номер порта не является числом. " + DEFAULT_PORT_MSG);
            return DEFAULT_SERVER_PORT;
        }
        if (userInputInt < MIN_SERVER_PORT || userInputInt > MAX_SERVER_PORT) {
            System.out.println("Введённый номер порта не входит в разрешенный диапазон номеров портов " +
                    MIN_SERVER_PORT + "-" + MAX_SERVER_PORT + ". " + System.lineSeparator() + DEFAULT_PORT_MSG);
            return DEFAULT_SERVER_PORT;
        }
        return userInputInt;
    }
}
