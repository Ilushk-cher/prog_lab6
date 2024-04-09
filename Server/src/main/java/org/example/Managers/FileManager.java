package org.example.Managers;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.example.CollectionModel.HumanBeing;
import org.example.CommandSpace.Printable;
import org.example.Exceptions.ExitProgram;
import org.example.Exceptions.InvalidForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Менеджер работы с файлом
 */
public class FileManager {
    private String text;
    private final String pathToFile;
    private final Printable console;
    private final CollectionManager collectionManager;
    private final XStream xStream = new XStream();
    static final Logger fileManagerLogger = LoggerFactory.getLogger(FileManager.class);

    public FileManager(Printable console, CollectionManager collectionManager, String pathToFile) {
        this.console = console;
        this.collectionManager = collectionManager;
        this.pathToFile = pathToFile;

        xStream.alias("HumanBeing", HumanBeing.class);
        xStream.alias("Array", CollectionManager.class);
        xStream.addPermission(AnyTypePermission.ANY);
        xStream.addImplicitCollection(CollectionManager.class, "collection");
        fileManagerLogger.info("Созданы алиасы для файла");
    }

    /**
     * Метод, проверяющий доступность файла
     */
    public void findFile() throws ExitProgram {
        File file = new File(pathToFile);
        BufferedReader bufferedReader;
        FileReader fileReader;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while (bufferedReader.ready()) {
                stringBuilder.append(bufferedReader.readLine());
            }
            fileReader.close();
            bufferedReader.close();
            fileManagerLogger.info("Файл прочитан");
            if (stringBuilder.isEmpty()) {
                console.printError("Передан пустой файл");
                fileManagerLogger.error("Передан пустой файл");
                text = "</Array>";
            }
            text = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            console.printError("Файл не найден");
            fileManagerLogger.error("Файл не найден");
            throw new ExitProgram();
        } catch (IOException e) {
            console.printError("Ошибка ввода/вывода " + e);
            fileManagerLogger.error("Ошибка ввода/вывода" + e);
            throw new ExitProgram();
        }
    }

    /**
     * Метод, считывающий коллекцию из файла
     */
    public void createObjects() throws ExitProgram {
        try {
            XStream xStreamCreate = new XStream();
            xStreamCreate.alias("HumanBeing", HumanBeing.class);
            xStreamCreate.alias("Array", CollectionManager.class);
            xStreamCreate.addPermission(AnyTypePermission.ANY);
            xStreamCreate.addImplicitCollection(CollectionManager.class, "collection");
            CollectionManager collectionManagerCreating = (CollectionManager) xStreamCreate.fromXML(text);
            this.collectionManager.setLastSaveTime(collectionManagerCreating.getLastSaveTimeInDate());
            this.collectionManager.setLastInitTime(collectionManagerCreating.getInitTimeInDate());
            if (collectionManagerCreating.getCollection() == null) {
                console.printError("Коллекция в файле пуста");
                fileManagerLogger.error("Коллекция в файле пуста");
                return;
            }
            for (HumanBeing humanBeing : collectionManagerCreating.getCollection()) {
                if (collectionManager.checkExistById(humanBeing.getId())) {
                    console.printError("Найдены повторяющиеся id в файле");
                    fileManagerLogger.error("Найдены повторяющиеся id в файле");
                    throw new ExitProgram();
                }
                if (!humanBeing.validate()) {
                    console.printError("Элемент с id " + humanBeing.getId() + " невалиден");
                    fileManagerLogger.error("Элемент с id " + humanBeing.getId() + " невалиден");
                    continue;
                }
                this.collectionManager.addElement(humanBeing);
            }
            this.collectionManager.setLastSaveTime(collectionManagerCreating.getLastSaveTimeInDate());
        } catch (StreamException e) {
            console.printError("Объекты из файла невалидны");
        } catch (com.thoughtworks.xstream.converters.ConversionException e) {
            console.printError("Нарушена структура xml файла с данными");
        }
        console.println("Получены объекты:\n" + collectionManager.getCollection().toString());
        fileManagerLogger.info("Объекты из файла загружены в коллекцию");
    }

    /**
     * Метод, записывающий коллекцию в файл
     */
    public void saveObjects() {
        String filePath = pathToFile;
        if (filePath == null || filePath.isEmpty()) {
            console.printError("Пустой путь недопустим");
            fileManagerLogger.error("Пустой путь недопустим");
            return;
        } else {
            console.println("Путь к файлу успешно получен");
            fileManagerLogger.info("Путь к файлу успешно получен");
        }
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(filePath));
            printWriter.write(xStream.toXML(collectionManager));
            printWriter.close();
            fileManagerLogger.info("Объекты коллекции сохранены в файл");
        } catch (FileNotFoundException e) {
            console.printError("Файл не найден");
            fileManagerLogger.error("Файл не найден");
        } catch (IOException e) {
            console.printError("Ошибка ввода/вывода");
            fileManagerLogger.error("Ошибка ввода/вывода");
        }

    }
}