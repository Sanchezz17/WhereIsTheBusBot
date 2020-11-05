package com.github.telegram.bot.utils;

public interface ValueGetter<T> {
    String getValue(T enumItem);
}
