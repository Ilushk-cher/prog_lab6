package org.example.Commands;

import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Connection.ResponseStatus;
import org.example.Exceptions.InvalidArguments;
import org.example.Managers.CollectionManager;

import java.util.Objects;

/**
 * Класс команды для обновления элемента коллекции по его id
 */
public class Update extends Command {
    private final CollectionManager collectionManager;

    public Update(CollectionManager collectionManager) {
        super("update", "id {element}", "обновить значение элемента коллекции, id которого равен заданному");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) throws InvalidArguments {
        if (request.getArgs().isBlank()) throw new InvalidArguments();
        try {
            int id = Integer.parseInt(request.getArgs().trim());
            if (!collectionManager.checkExistById(id)) throw new NoSuchId();
            if (Objects.isNull(request.getHumanBeing())) {
                return new Response(ResponseStatus.ASKING_OBJECT, "Для выполнения команды " + getName() + " нужен объект");
            }
            collectionManager.editById(id, request.getHumanBeing());
            return new Response(ResponseStatus.OK, "Объект успешно обновлен");
        } catch (NoSuchId e) {
            return new Response(ResponseStatus.ERROR, "Нет элемента с таким id");
        } catch (NumberFormatException e) {
            return new Response(ResponseStatus.WRONG_ARGS, "id должен быть числом типа long");
        }
    }
}