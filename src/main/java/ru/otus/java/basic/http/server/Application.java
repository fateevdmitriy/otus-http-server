package ru.otus.java.basic.http.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.*;

public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);
    private static final String PROPERTY_FILE_NAME = "otus-http-server.properties";
    private static Properties properties;
    private static final List<String> propertyNames = Arrays.asList(
            "defaultServerPort", "minServerPort", "maxServerPort", "threadPoolSize", "httpRequestSizeLimit",
            "httpResponseSizeLimit", "clientHandlerBufferSize", "protocolVersionPrefix", "httpProtocolVersion");

    public static int getDefaultServerPort() {
        return Integer.parseInt(properties.getProperty("defaultServerPort").trim());
    }

    public static int getMinServerPort() {
        return Integer.parseInt(properties.getProperty("minServerPort").trim());
    }

    public static int getMaxServerPort() {
        return Integer.parseInt(properties.getProperty("maxServerPort").trim());
    }

    public static int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("threadPoolSize").trim());
    }

    public static int getHttpRequestSizeLimit() {
        return Integer.parseInt(properties.getProperty("httpRequestSizeLimit").trim());
    }

    public static int getHttpResponseSizeLimit() {
        return Integer.parseInt(properties.getProperty("httpResponseSizeLimit").trim());
    }

    public static int getClientHandlerBufferSize() {
        return Integer.parseInt(properties.getProperty("clientHandlerBufferSize").trim());
    }

    public static String getProtocolVersionPrefix() {
        return properties.getProperty("protocolVersionPrefix").trim();
    }

    public static String getHttpVersion() {
        return getProtocolVersionPrefix() + '/' + properties.getProperty("httpProtocolVersion").trim();
    }

    public static void main(String[] args) {
        try {
            readPropertiesFromFile();
            int serverPort = askUserForServerPort();
            new HttpServer(serverPort).start(getThreadPoolSize());
        } catch (IOException e) {
            logger.error("Файл свойств '{}' не найден или не содержит данных. {}", PROPERTY_FILE_NAME, e.getMessage());
            e.printStackTrace();
        }
    }

    private static void readPropertiesFromFile() throws IOException {
        File propertyFile = new File(PROPERTY_FILE_NAME);
        if (!propertyFile.exists() || propertyFile.isDirectory()) {
            throw new FileNotFoundException("Файл '" + PROPERTY_FILE_NAME + "' не существует.");
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(PROPERTY_FILE_NAME), StandardCharsets.UTF_8))) {
            properties = new Properties();
            properties.load(in);
            for (String propName: propertyNames) {
                if (!properties.containsKey(propName)) {
                    throw new InvalidParameterException("Файл конфигурации '" + PROPERTY_FILE_NAME + "' не содержит необходимого свойства '" + propName +"'.");
                } else if (properties.getProperty(propName).isEmpty()) {
                    throw new InvalidParameterException("Файл конфигурации '" + PROPERTY_FILE_NAME + "' не содержит значения свойства '" + propName +"'.");
                }
            }
        }
    }

    private static int askUserForServerPort() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите номер порта на котором будет работать запускаемый HTTP-сервер."
                + System.lineSeparator() + "Номер порта должен быть в диапазоне " + getMinServerPort() + " - " + getMaxServerPort() + ":"
                + System.lineSeparator() + "Если указать пустой порт, будет использован номер порта по-умолчанию, из файла свойств.");
        String userInputStr = scanner.nextLine();
        if (userInputStr.isEmpty()) {
            logger.error("Не задан номер порта сервера. Будет использоваться порт по-умолчанию: {}", getDefaultServerPort());
            return getDefaultServerPort();
        }
        int serverPort;
        serverPort = Integer.parseInt(userInputStr.trim());
        if (serverPort < getMinServerPort() || serverPort > getMaxServerPort()) {
            throw new InputMismatchException("Введённый номер порта не входит в разрешенный диапазон номеров портов " + getMinServerPort() + "-" + getMaxServerPort() + ".");
        }
        return serverPort;
    }
}
