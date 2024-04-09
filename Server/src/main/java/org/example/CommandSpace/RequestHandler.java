package org.example.CommandSpace;

import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Connection.ResponseStatus;
import org.example.Exceptions.CommandRuntimeError;
import org.example.Exceptions.ExitProgram;
import org.example.Exceptions.InvalidArguments;
import org.example.Exceptions.NoSuchCommand;
import org.example.Managers.CommandManager;

public class RequestHandler {
    private final CommandManager commandManager;

    public RequestHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public Response handle(Request request) {
        try {
            commandManager.addToHistory(request.getCommandName());
            return commandManager.execute(request);
        } catch (InvalidArguments e) {
            return new Response(ResponseStatus.WRONG_ARGS, "Неверно использованы аргументы для команды");
        } catch (CommandRuntimeError e) {
            return new Response(ResponseStatus.ERROR, "Ошибка исполнения команды");
        } catch (NoSuchCommand e) {
            return new Response(ResponseStatus.ERROR, "Такой команды нет в списке");
        } catch (ExitProgram e) {
            return new Response(ResponseStatus.EXIT);
        }
    }
}
