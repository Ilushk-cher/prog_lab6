package org.example.Managers;

import org.example.Commands.Command;
import org.example.Commands.Interfaces.Editor;
import org.example.Connection.Request;
import org.example.Connection.Response;
import org.example.Exceptions.CommandRuntimeError;
import org.example.Exceptions.ExitProgram;
import org.example.Exceptions.InvalidArguments;
import org.example.Exceptions.NoSuchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс менеджера комманд, содержащий информацию о них и их историю
 */
public class CommandManager {
    private final HashMap<String, Command> commandsHashMap = new HashMap<>();
    private final List<String> commandHistory = new ArrayList<>();
    private final FileManager fileManager;
    static final Logger commandManagerLogger = LoggerFactory.getLogger(CommandManager.class);

    public CommandManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void addCommandToHashMap(Command command) {
        this.commandsHashMap.put(command.getName(), command);
        commandManagerLogger.info("Новая команда в списке доступных");
    }

    public void addCommandToHashMap(Collection<Command> commands) {
        this.commandsHashMap.putAll(commands.stream().collect((Collectors.toMap(Command::getName, s -> s))));
        commandManagerLogger.info("Новые команды в списке доступных");
    }

    public Collection<Command> getCommandsHashMap() {
        return commandsHashMap.values();
    }

    public void addToHistory(String line) {
        if (line.isBlank()) return;
        this.commandHistory.add(line);
    }

    public List<String> getCommandHistory() {
        return commandHistory;
    }

    public Response execute(Request request) throws ExitProgram, InvalidArguments, NoSuchCommand, CommandRuntimeError {
        Command command = commandsHashMap.get(request.getCommandName());
        if (command == null) {
            throw new NoSuchCommand();
        }
        Response response = command.execute(request);
        fileManager.saveObjects();
        return response;
    }
}
