package org.example.Commands;

import org.example.Commands.Interfaces.Editor;
import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Connection.ResponseStatus;
import org.example.Exceptions.InvalidArguments;
import org.example.Managers.CollectionManager;

import java.util.Objects;

/**
 * Класс команды создания нового элемента и добавления его в коллекцию
 */
public class AddElement extends Command implements Editor {
    private final CollectionManager collectionManager;

    public AddElement(CollectionManager collectionManager) {
        super("add", "добавить новый элемент в коллекцию");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) throws InvalidArguments {
        if (!request.getArgs().isBlank()) throw new InvalidArguments();
        if (Objects.isNull(request.getHumanBeing())) {
            return new Response(ResponseStatus.ASKING_OBJECT, "Для выполнения команды " + getName() + " нужен объект");
        } else {
            request.getHumanBeing().setId(CollectionManager.incIdPoint());
            collectionManager.addElement(request.getHumanBeing());
            return new Response(ResponseStatus.OK, "Объект успешно добавлен");
        }
    }
}
