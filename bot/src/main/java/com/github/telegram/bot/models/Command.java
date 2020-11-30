package com.github.telegram.bot.models;

public enum Command {
    START_OVER("Начать заново"),
    ADD_TO_FAVORITE("Добавить в избранное"),
    NEW("Новый запрос"),
    SHOW_FAVORITE("Избранное");
    
    public static Command[] endCommands = new Command[] { Command.START_OVER, Command.ADD_TO_FAVORITE };

    public static Command[] startCommands = new Command[] { Command.NEW, Command.SHOW_FAVORITE };

    private final String name;

    Command(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static Command fromString(String text) {
        for (Command command : Command.values()) {
            if (command.name.equalsIgnoreCase(text)) {
                return command;
            }
        }
        return null;
    }
}
