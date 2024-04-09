package org.example.Managers;

import org.example.Client;
import org.example.CollectionModel.HumanBeing;
import org.example.CommandSpace.Console;
import org.example.CommandSpace.ExecuteFileSpace;
import org.example.CommandSpace.Forms.HumanBeingForm;
import org.example.CommandSpace.Printable;
import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Connection.ResponseStatus;
import org.example.Exceptions.ConnectionException;
import org.example.Exceptions.ExitProgram;
import org.example.Exceptions.FileModeException;
import org.example.Exceptions.InvalidForm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Класс менеджера работы приложения
 */
public class RuntimeManager {
    private final Printable console;
    private final Scanner userScanner;
    private final Client client;
    private final List<String> commandHistory = new ArrayList<>();

    public RuntimeManager(Printable console, Scanner userScanner, Client client) {
        this.console = console;
        this.userScanner = userScanner;
        this.client = client;
    }

    /**
     * Метод, запускающий интерактивный режим работы приложения
     */
    public void runInteractive() {
        Scanner userScanner = ScannerManager.getUserScanner();
        while (true) {
            try {
                if (!userScanner.hasNext()) throw new ExitProgram();
                String[] userCommand = (userScanner.nextLine().trim() + " ").split(" ", 2);
                Response response = client.getResponse(new Request(userCommand[0].trim(), userCommand[1].trim()));
                commandHistory.add(userCommand[0].trim());
                printResponse(response);
                switch (response.getResponseStatus()) {
                    case ASKING_OBJECT -> {
                        HumanBeing humanBeing = new HumanBeingForm(console).build();
                        Response newResponse = client.getResponse(
                                new Request(userCommand[0].trim(), userCommand[1].trim(), humanBeing)
                        );
                        if (newResponse.getResponseStatus() != ResponseStatus.OK) {
                            console.printError(newResponse.getResponse());
                        } else {
                            printResponse(newResponse);
                        }
                    }
                    case EXIT -> throw new ExitProgram();
                    case EXECUTE_SCRIPT -> {
                        Console.setFileMode(true);
                        executeFile(response.getResponse());
                        Console.setFileMode(false);
                    }
                    case HISTORY -> console.println(String.join("\n", commandHistory.subList(Math.max(commandHistory.size() - 9, 0), commandHistory.size())));
                    default -> {}
                }
            } catch (NoSuchElementException e) {
                console.printError("Пользовательский ввод не обнаружен");
            } catch (ExitProgram e) {
                console.println("До свидания!");
                return;
            }
        }
    }

    private void printResponse(Response response) {
        switch (response.getResponseStatus()) {
            case OK -> {
                if (Objects.isNull(response.getCollection())) {
                    console.println(response.getResponse());
                } else {
                    console.println(response.getResponse() + "\n" + response.getCollection().toString());
                }
            }
            case ERROR -> console.printError(response.getResponse());
            case WRONG_ARGS -> console.printError("Проверьте аргументы команды");
            default -> {}
        }
    }

    private void executeFile(String args) throws ExitProgram {
        if (args == null || args.isEmpty()) {
            console.printError("Отсутствует путь к исполняемому файлу");
            return;
        } else console.println("Путь к файлу успешно получен");
        args = args.trim();
        try {
            ExecuteFileSpace.pushFile(args);
            for (String line = ExecuteFileSpace.readLine(); line != null; line = ExecuteFileSpace.readLine()) {
                String[] command = (line.trim() + " ").split(" ", 2);
                command[1] = command[1].trim();
                if (command[0].isBlank()) return;
                if (command[0].equals("execute_script")) {
                    if (ExecuteFileSpace.isFileRepeat(command[1])) {
                        console.printError("Обнаружена рекурсия. Путь: " + new File(command[1]).getAbsolutePath());
                        continue;
                    }
                }
                console.println("Выполняется команда: " + command[0]);
                Response response = client.getResponse(new Request(command[0], command[1]));
                printResponse(response);
                switch (response.getResponseStatus()) {
                    case ASKING_OBJECT -> {
                        HumanBeing humanBeing;
                        try {
                            humanBeing = new HumanBeingForm(console).build();
                            if (!humanBeing.validate()) throw new FileModeException();
                        } catch (FileModeException exc) {
                            console.printError("Поля из файла невалидны");
                            continue;
                        }
                        Response newResponse = client.getResponse(
                                new Request(command[0].trim(), command[1].trim(), humanBeing)
                        );
                        if (newResponse.getResponseStatus() != ResponseStatus.OK) {
                            console.printError(newResponse.getResponse());
                        } else printResponse(newResponse);
                    }
                    case EXIT -> throw new ExitProgram();
                    case EXECUTE_SCRIPT -> {
                        executeFile(response.getResponse());
                        ExecuteFileSpace.popRecursion();
                    }
                    default -> {}
                }
            }
            ExecuteFileSpace.popFile();
        } catch (FileNotFoundException e) {
            console.printError("Файл не найден");
        } catch (IOException e) {
            console.printError("Ошибка ввода/вывода");
        }
    }
}
