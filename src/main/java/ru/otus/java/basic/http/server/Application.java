package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
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
        int serverPort = defaultServerPort;
        try {
            initParamsFromProperties(PROPERTY_FILE_NAME);
            serverPort = askUserForServerPort();
            new HttpServer(serverPort).start(threadPoolSize);
        } catch (IOException e) {
            logger.error("Файл свойств '{}' не найден или не содержит данных. {}", PROPERTY_FILE_NAME, e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initParamsFromProperties(String propertyFileName) throws IOException {
        Properties properties = null;
        File propertyFile = new File(propertyFileName);
        if (!propertyFile.exists() || propertyFile.isDirectory()) {
            throw new IOException("Файл '" + propertyFileName + "' не существует.");
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(propertyFileName), StandardCharsets.UTF_8))) {
            properties = new Properties();
            properties.load(in);
            if (!properties.containsKey("defaultServerPort")
                    || !properties.containsKey("minServerPort")
                    || !properties.containsKey("maxServerPort")
                    || !properties.containsKey("threadPoolSize")
                    || !properties.containsKey("httpRequestSizeLimit")
                    || !properties.containsKey("httpResponseSizeLimit")) {
                throw new IOException("Файл '" + propertyFileName + "' не содержит всех необходимых свойств.");
            }
            defaultServerPort = Integer.parseInt(properties.getProperty("defaultServerPort").trim());
            minServerPort = Integer.parseInt(properties.getProperty("minServerPort").trim());
            maxServerPort = Integer.parseInt(properties.getProperty("maxServerPort").trim());
            threadPoolSize = Integer.parseInt(properties.getProperty("threadPoolSize").trim());
            httpRequestSizeLimit = Integer.parseInt(properties.getProperty("httpRequestSizeLimit").trim());
            httpResponseSizeLimit = Integer.parseInt(properties.getProperty("httpResponseSizeLimit").trim());
        }
    }

    private static int askUserForServerPort() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите номер порта на котором будет работать запускаемый HTTP-сервер." +
                System.lineSeparator() + "Номер порта должен быть в диапазоне " + minServerPort + "-" + maxServerPort +":");
        String userInputStr = scanner.nextLine();
        if (userInputStr.isEmpty()) {
            logger.error("Не задан номер порта сервера. Будет использоваться порт по-умолчанию, указанный в файле свойств.");
            return defaultServerPort;
        }
        int serverPort;
        serverPort = Integer.parseInt (userInputStr.trim());
        if (serverPort < minServerPort || serverPort > maxServerPort) {
            throw new InputMismatchException("Введённый номер порта не входит в разрешенный диапазон номеров портов " + minServerPort + "-" + maxServerPort +".");
        }
        return serverPort;
    }
}
