package com.github.telegram.bot.controllers;

import com.github.telegram.bot.models.Command;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class StartController {
    private static final Logger log = LogManager.getLogger(StartController.class);

    @BotRequest("/start")
    private SendMessage start(String text,
                      Long chatId,
                      TelegramRequest telegramRequest,
                      TelegramBot telegramBot,
                      Update update,
                      Message message,
                      Chat chat,
                      User user
    ) {
        log.info("Пользователь начал взаимодействие с ботом. userId: " + user.id());
        SendMessage hello = new SendMessage(chatId, "Привет! Я - бот, который может подсказать" +
                " через сколько минут приедет общественный транспорт на определенную остановку");
        telegramBot.execute(hello);
        return sendStartPrompt(chatId);
    }

    @BotRequest(value = "/command START_OVER*", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage sendStartPrompt(Long chatId) {
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                Command.startCommands,
                Command::getName,
                Command::toString,
                "command",
                2);
        return new SendMessage(chatId, "Выберите действие").replyMarkup(inlineKeyboardMarkup);
    }

    @BotRequest(value = "/command NEW*", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage sendTransportPrompt(Long chatId) {
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                Transport.values(),
                Transport::getName,
                Transport::getName,
                "transport",
                2);
        return new SendMessage(chatId, "Выберите вид общественного транспорта")
                .replyMarkup(inlineKeyboardMarkup);
    }
}
