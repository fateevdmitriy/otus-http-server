package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);
    private static final String PROPERTY_FILE_NAME = "otus-http-server.properties";
    private static int minServerPort;
    private static int maxServerPort;
    private static int defaultServerPort;
    private static int threadPoolSize;
    private static int httpRequestSizeLimit;
    private static int httpResponseSizeLimit;

    public static int getHttpRequestSizeLimit() {
        return httpRequestSizeLimit;
    }

    public static int getHttpResponseSizeLimit() {
        return httpResponseSizeLimit;
    }

    public static void main(String[] args) {
        Properties propsRepository;
        propsRepository = readPropertiesFile(PROPERTY_FILE_NAME);
        if (propsRepository == null) { return; }
        try {
            defaultServerPort = Integer.parseInt(propsRepository.getProperty("defaultServerPort").trim());
            minServerPort = Integer.parseInt(propsRepository.getProperty("minServerPort").trim());
            maxServerPort = Integer.parseInt(propsRepository.getProperty("maxServerPort").trim());
            threadPoolSize = Integer.parseInt(propsRepository.getProperty("threadPoolSize").trim());
            httpRequestSizeLimit = Integer.parseInt(propsRepository.getProperty("httpRequestSizeLimit").trim());
            httpResponseSizeLimit = Integer.parseInt(propsRepository.getProperty("httpResponseSizeLimit").trim());
        } catch (NumberFormatException e) {
            logger.error("Ошибка преобразования строкового значения свойства из файла свойств в целое число. {}", e.getMessage());
            e.printStackTrace();
        }
        int serverPort = defaultServerPort;
        try {
            serverPort = askServerPort();
        } catch (IOException e) {
            logger.error("{} Будет использованы параметр из файла свойств.", e.getMessage());
        }
        new HttpServer(serverPort).start(threadPoolSize);
    }

    private static int askServerPort() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите номер порта на котором будет работать запускаемый HTTP-сервер." +
                System.lineSeparator() + "Номер порта должен быть в диапазоне " + minServerPort + "-" + maxServerPort +":");
        String userInputStr = scanner.nextLine();
        if (userInputStr.isEmpty()) {
            throw new IOException("Не задан номер порта сервера.");
        }
        int serverPort;
        try {
            serverPort = Integer.parseInt (userInputStr.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Введённый номер порта не является числом.");
        }
        if (serverPort < minServerPort || serverPort > maxServerPort) {
            throw new IOException("Введённый номер порта не входит в разрешенный диапазон номеров портов " + minServerPort + "-" + maxServerPort +".");
        }
        return serverPort;
    }

    private static Properties readPropertiesFile(String fileName) {
        Properties properties = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            properties = new Properties();
            properties.load(in);
        } catch (FileNotFoundException e) {
            logger.error("Файл '{}' не найден. Невозможно прочитать параметры программы из файла свойств. ", fileName);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Возникло исключение при чтении файла свойств программы. ");
            e.printStackTrace();
        }
        return properties;
    }

}
