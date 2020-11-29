package com.github.telegram.bot.models;

public enum Command {
    NEW("Начать заново"),
    ADD_TO_FAVORITE("Добавить в избранное");

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
