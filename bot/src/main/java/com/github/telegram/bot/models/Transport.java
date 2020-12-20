package com.github.telegram.bot.models;

public enum Transport {
    TRAM("Трамвай", "\uD83D\uDE8A"),
    TROLLEYBUS("Троллейбус", "\uD83D\uDE8E");

    private final String name;

    private final String emoji;

    Transport(String name, String emoji) {
        this.name = name;
        this.emoji =  emoji;
    }

    public String getName() {
        return this.name;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public static Transport fromString(String str) {
        for (Transport transport : Transport.values()) {
            if (transport.name.equalsIgnoreCase(str)) {
                return transport;
            }
        }
        return null;
    }
}
