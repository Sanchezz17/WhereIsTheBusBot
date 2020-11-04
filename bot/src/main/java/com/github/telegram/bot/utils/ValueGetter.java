package com.github.telegram.bot.utils;

public interface ValueGetter<T extends Enum<T>> {
    String getValue(T enumItem);
}
