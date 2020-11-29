package com.github.telegram.bot.models;

@SuppressWarnings("NonAsciiCharacters")
public enum FirstLetter {
    One("1"),
    Four("4"),
    Seven("7"),
    А("А"),
    Б("Б"),
    В("В"),
    Г("Г"),
    Д("Д"),
    Е("Е"),
    Ж("Ж"),
    З("З"),
    И("И"),
    К("К"),
    Л("Л"),
    М("М"),
    Н("Н"),
    О("О"),
    П("П"),
    Р("Р"),
    С("С"),
    Т("Т"),
    У("У"),
    Ф("Ф"),
    Х("Х"),
    Ц("Ц"),
    Ч("Ч"),
    Ш("Ш"),
    Щ("Щ"),
    Э("Э"),
    Ю("Ю"),
    Я("Я");

    private final String value;

    FirstLetter(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static FirstLetter fromString(String otherValue) {
        for (FirstLetter letter : FirstLetter.values()) {
            if (letter.value.equalsIgnoreCase(otherValue)) {
                return letter;
            }
        }
        return null;
    }
}
