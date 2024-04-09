package org.example;

import org.example.CommandSpace.BlankConsole;
import org.example.CommandSpace.Console;
import org.example.CommandSpace.Printable;
import org.example.CommandSpace.RequestHandler;
import org.example.Commands.*;
import org.example.Exceptions.ExitProgram;
import org.example.Managers.CollectionManager;
import org.example.Managers.CommandManager;
import org.example.Managers.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.security.spec.ECField;
import java.util.Collection;
import java.util.IllegalFormatPrecisionException;
import java.util.List;
import java.util.logging.LogManager;

public class Main extends Thread {
    public static int port = 9878;
    public static final int connectionTimeout = 60 * 1000;
    private static final Printable console = new BlankConsole();
    static final Logger mainLogger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        CollectionManager collectionManager = new CollectionManager();
        try {
            String pathToFile = args[0];
        } catch (IndexOutOfBoundsException e) {
            console.printError("Передайте путь в аргументе!");
            mainLogger.error("Путь к файлу с коллекцией не передан в аргументе");
            return;
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {}
        }
        FileManager fileManager = new FileManager(console, collectionManager, args[0]);

        try {
            fileManager.findFile();
            fileManager.createObjects();
        } catch (ExitProgram e) {
            console.println("До свидания");
            mainLogger.error("Экстренное завершение работы");
            return;
        }

        CommandManager commandManager = new CommandManager(fileManager);
        commandManager.addCommandToHashMap(List.of(
                new Help(commandManager),
                new Info(collectionManager),
                new Show(collectionManager),
                new AddElement(collectionManager),
                new Update(collectionManager),
                new RemoveById(collectionManager),
                new Clear(collectionManager),
                new Execute(),
                new Exit(),
                new RemoveLast(collectionManager),
                new AddIfMax(collectionManager),
                new History(commandManager),
                new SumOfImpactSpeed(collectionManager),
                new FilterGreaterThanImpactSpeed(collectionManager),
                new PrintUniqueMood(collectionManager)
        ));

        RequestHandler requestHandler = new RequestHandler(commandManager);
        Server server = new Server(port, connectionTimeout, requestHandler, fileManager);
        mainLogger.info("Запуск сервера");
        server.run();
    }
}