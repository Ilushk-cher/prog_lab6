package org.example.Commands;

import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Connection.ResponseStatus;
import org.example.Exceptions.InvalidArguments;
import org.example.Managers.CollectionManager;

/**
 * Класс команды для удаления элемента коллекции по его id
 */
public class RemoveById extends Command {
    private final CollectionManager collectionManager;

    public RemoveById(CollectionManager collectionManager) {
        super("remove_by_id", "id","удалить элемент из коллекции по его id");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) throws InvalidArguments {
        if (request.getArgs().isBlank()) throw new InvalidArguments();
        try {
            long id = Long.parseLong(request.getArgs().trim());
            if (!collectionManager.checkExistById(id)) throw new NoSuchId();
            collectionManager.removeElement(collectionManager.getById(id));
            return new Response(ResponseStatus.OK, "Элемент удален успешно");
        } catch (NoSuchId e) {
            return new Response(ResponseStatus.ERROR, "Нет элемента с таким id");
        } catch (NumberFormatException e) {
            return new Response(ResponseStatus.WRONG_ARGS, "id должен быть числом типа long");
        }
    }
}
