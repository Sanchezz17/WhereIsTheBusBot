package com.github.telegram.bot.models;

public enum Transport {
    Trolleybus("Троллейбус"),
    Tram("Трамвай");

    private final String name;

    Transport(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
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
