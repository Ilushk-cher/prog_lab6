package org.example;

import org.example.CommandSpace.BlankConsole;
import org.example.CommandSpace.Console;
import org.example.CommandSpace.Printable;
import org.example.CommandSpace.RequestHandler;
import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Exceptions.ConnectionErrorException;
import org.example.Exceptions.OpenServerException;
import org.example.Managers.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.ClientInfoStatus;

public class Server {
    private int port;
    private int socketTimeout;
    private Printable console;
    private ServerSocketChannel serverSocketChannel;
    private SocketChannel socketChannel;
    private RequestHandler requestHandler;

    static final Logger serverLogger = LoggerFactory.getLogger(Server.class);

    BufferedReader scanner = new BufferedReader(new InputStreamReader(new BufferedInputStream(System.in)));
    private FileManager fileManager;

    public Server(int port, int socketTimeout, RequestHandler requestHandler, FileManager fileManager) {
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.requestHandler = requestHandler;
        this.fileManager = fileManager;
        this.console = new BlankConsole();
    }

    public void run() {
        try {
            openServerSocket();
            serverLogger.info("Создано соединение с клиентом");
            while (true) {
                try {
                    if (scanner.ready()) {
                        String inputLine = scanner.readLine();
                        if (inputLine.equals("save")) {
                            fileManager.saveObjects();
                            serverLogger.info("Коллекция сохранена");
                        }
                    }
                } catch (IOException e) {
                } catch (NullPointerException e) {
                    console.printError("Остановка сервера");
                    serverLogger.error("Остановка сервера");
                    fileManager.saveObjects();
                    return;
                }
                try (SocketChannel clientSocket = connectToClient()) {
                    if (!processClientRequest(clientSocket)) break;
                } catch (ConnectionErrorException | SocketTimeoutException e) {
                } catch (IOException e) {
                    console.printError("Ошибка при попытке завершения соединения с клиентом");
                    serverLogger.error("Ошибка при попытке завершения соединения с клиентом");
                }
            }
            stop();
            serverLogger.info("Закрыто соединение с клиентом");
        } catch (OpenServerException e) {
            console.printError("Невозможно запустить сервер");
            serverLogger.error("Невозможно запустить сервер");
        }
    }

    private void openServerSocket() throws OpenServerException {
        try {
            SocketAddress socketAddress = new InetSocketAddress(port);
            serverLogger.info("Создан соккет");
            serverSocketChannel = ServerSocketChannel.open();
            serverLogger.info("Создан канал");
            serverSocketChannel.bind(socketAddress);
            serverLogger.info("Открыт канал-соккет");
        } catch (IllegalArgumentException e) {
            console.printError("Порт находится за пределами возможных значений");
            serverLogger.info("Порт находится за пределами возможных значений");
            throw new OpenServerException();
        } catch (IOException e) {
            console.printError("При попытке использовать порт " + port + " возникла ошибка");
            serverLogger.error("При попытке использовать порт " + port + " возникла ошибка");
            throw new OpenServerException();
        }
    }

    private SocketChannel connectToClient() throws ConnectionErrorException, SocketTimeoutException {
        try {
            serverSocketChannel.socket().setSoTimeout(100);
            socketChannel = serverSocketChannel.socket().accept().getChannel();
            console.println("Соединение с клиентом установлено");
            serverLogger.info("Соединение с клиентом установлено");
            return socketChannel;
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException();
        } catch (IOException e) {
            serverLogger.error("Ошибка соединения с клиентом");
            throw new ConnectionErrorException();
        }
    }

    private boolean processClientRequest(SocketChannel clientSocket) {
        Request userRequest = null;
        Response responseToUser = null;
        try {
            ObjectInputStream clientReader = new ObjectInputStream(clientSocket.socket().getInputStream());
            ObjectOutputStream clientWriter = new ObjectOutputStream(clientSocket.socket().getOutputStream());
            serverLogger.info("Открыты потоки ввода/вывода");
            do {
                userRequest = (Request) clientReader.readObject();
                serverLogger.info("Получен запрос с командой" + userRequest.getCommandName() + ": " + userRequest);
                console.println(userRequest.toString());
                responseToUser = requestHandler.handle(userRequest);
                clientWriter.writeObject(responseToUser);
                serverLogger.info("Клиенту отправлен ответ: " + responseToUser);
                clientWriter.flush();
            } while (true);
        } catch (ClassNotFoundException e) {
            console.printError("Ошибка при получении данных");
            serverLogger.error("Ошибка при получении данных");
        } catch (InvalidClassException | NotSerializableException e) {
            console.printError("Ошибка отправки данных клиенту");
            serverLogger.error("Ошибка отправки данных клиенту");
        } catch (IOException e) {
            if (userRequest == null) {
                console.printError("Внезапный разрыв соединения с клиентом");
                serverLogger.error("незапный разрыв соединения с клиентом");
            } else {
                console.printError("Клиент успешно отключен от сервера");
                serverLogger.info("Клиент успешно отключен от сервера");
            }
        }
        return true;
    }

    private void stop() {
        class ClosedSocketException extends Exception {}
        try {
            if (socketChannel == null) throw new ClosedSocketException();
            socketChannel.close();
            serverSocketChannel.close();
            serverLogger.info("Все соединения закрыты");
        } catch (ClosedSocketException e) {
            console.printError("Сервер еще не запущен => невозможно завершить его работу");
            serverLogger.error("Сервер еще не запущен => невозможно завершить его работу");
        } catch (IOException e) {
            console.printError("Ошибка завершения работы сервера");
            serverLogger.error("Ошибка завершения работы сервера");
        }
    }
}
