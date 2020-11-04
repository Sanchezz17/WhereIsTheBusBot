package com.github.telegram.bot.utils;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

public class KeyboardHelper {
    public static <T> InlineKeyboardMarkup getInlineKeyboardFromItems(
            T[] items,
            ValueGetter<T> valueGetter,
            ValueGetter<T> callbackDataGetter,
            String commandName,
            int chunkSize
    ) {
        int itemsCount = items.length;
        InlineKeyboardButton[][] buttonRows = new InlineKeyboardButton[(int) Math.ceil(itemsCount / (double) chunkSize)][];
        for (int i = 0; i < itemsCount; i++) {
            if (i % chunkSize == 0) {
                buttonRows[i / chunkSize] = new InlineKeyboardButton[Math.min(chunkSize, itemsCount - i)];
            }
            String callbackData = String.format("/%s %s", commandName, callbackDataGetter.getValue(items[i]));
            String value = valueGetter.getValue(items[i]);
            InlineKeyboardButton button = new InlineKeyboardButton(value).callbackData(callbackData);
            buttonRows[i / chunkSize][i % chunkSize] = button;
        }
        return new InlineKeyboardMarkup(buttonRows);
    }
}
