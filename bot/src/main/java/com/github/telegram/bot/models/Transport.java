package com.github.telegram.bot.models;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public enum Transport {
    TRAM("Трамвай", "\uD83D\uDE8A"),
    TROLLEYBUS("Троллейбус", "\uD83D\uDE8E");

    private final String name;

    private String emoji;

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

    public static Transport fromString(String text) {
        for (Transport transport : Transport.values()) {
            if (transport.name.equalsIgnoreCase(text)) {
                return transport;
            }
        }
        return null;
    }
}
