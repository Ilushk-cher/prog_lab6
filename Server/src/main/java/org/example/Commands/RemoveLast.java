package org.example.Commands;

import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Connection.ResponseStatus;
import org.example.Exceptions.InvalidArguments;
import org.example.Managers.CollectionManager;

import java.util.EmptyStackException;

/**
 * Класс команды для удаления последнего элемента коллекции
 */
public class RemoveLast extends Command {
    private final CollectionManager collectionManager;

    public RemoveLast(CollectionManager collectionManager) {
        super("remove_last", "удалить последний элемент из коллекции");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) throws InvalidArguments {
        if (!request.getArgs().isBlank()) throw new InvalidArguments();
        try {
            collectionManager.removeLast();
            return new Response(ResponseStatus.OK, "Элемент успешно удален");
        } catch (EmptyStackException e) {
            return new Response(ResponseStatus.ERROR, "Коллекция пуста");
        }
    }
}
