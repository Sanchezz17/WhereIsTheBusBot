package com.github.telegram.bot.models;

public enum Command {
    START_OVER("Начать заново"),
    ADD_TO_FAVORITE("Добавить в избранное"),
    REMOVE_FROM_FAVORITE("Удалить из избранного"),
    NEW("Новый запрос"),
    SHOW_FAVORITE("Избранное");

    public static Command[] startCommands = new Command[] { Command.NEW, Command.SHOW_FAVORITE };

    private final String name;

    Command(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
