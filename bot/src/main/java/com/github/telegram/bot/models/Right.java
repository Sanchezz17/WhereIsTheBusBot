package com.github.telegram.bot.models;

public enum Right {
    Admin("admin");

    private final String name;

    Right(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static Right fromString(String str) {
        for (Right right : Right.values()) {
            if (right.name.equalsIgnoreCase(str)) {
                return right;
            }
        }
        return null;
    }
}
