package ru.otus.java.basic.http.server;

import java.util.Scanner;

public class Application {
    private static final int MIN_SERVER_PORT = 8180;
    private static final int MAX_SERVER_PORT = 8190;
    private static final int DEFAULT_SERVER_PORT = 8189;
    private static final int THREADS_NUM = 10;

    public static void main(String[] args) {
        int serverPort = requestValidServerPost();
        new HttpServer(serverPort).start(THREADS_NUM);
    }

    private static int requestValidServerPost() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите номер порта на котором будет работать запускаемый HTTP-сервер." +
                System.lineSeparator() + "Номер порта должен быть в диапазоне " + MIN_SERVER_PORT + "-" + MAX_SERVER_PORT +":");
        String userInputStr = scanner.nextLine();
        if (userInputStr.isEmpty()) {
            System.out.println("Не задан номер порта сервера. " +
                    "Сервер запустится с номером порта по умолчанию: " + DEFAULT_SERVER_PORT);
            return DEFAULT_SERVER_PORT;
        }
        int userInputInt = DEFAULT_SERVER_PORT;
        try {
            userInputInt = Integer.parseInt (userInputStr.trim());
        } catch (NumberFormatException e) {
            System.out.println("Введённый номер порта не является числом. " +
                     "Сервер запустится с номером порта по умолчанию: "+ DEFAULT_SERVER_PORT);
            return DEFAULT_SERVER_PORT;
        }
        if (userInputInt < MIN_SERVER_PORT || userInputInt > MAX_SERVER_PORT) {
            System.out.println("Введённый номер порта не входит в разрешенный диапазон номеров портов " +
                    MIN_SERVER_PORT + "-" + MAX_SERVER_PORT + ". " + System.lineSeparator() +
                    "Сервер запустится с номером порта по умолчанию: "+ DEFAULT_SERVER_PORT);
            return DEFAULT_SERVER_PORT;
        }
        return userInputInt;
    }
}
