package com.github.telegram.bot.controllers;

import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.TelegramRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class StartController {
    private static final Logger logger = LoggerFactory.getLogger(StartController.class);

    @BotRequest("/start")
    SendMessage start(String text,
                      Long chatId,
                      TelegramRequest telegramRequest,
                      TelegramBot telegramBot,
                      Update update,
                      Message message,
                      Chat chat,
                      User user
    ) {
        logger.info("Text = {}", text);
        logger.info("ChatId or UserId = {}", chatId);
        logger.info("Telegram Request = {}", telegramRequest);
        logger.info("TelegramBot = {}", telegramBot);
        logger.info("Update = {}", update);
        logger.info("Message = {}", message);
        logger.info("Chat = {}", chat);
        logger.info("User = {}", user);

        SendMessage hello = new SendMessage(chatId, "Привет! Я - бот, который может подсказать" +
                " через сколько минут приедет общественный транспорт на определенную остановку");
        telegramBot.execute(hello);
        return sendTransportPrompt(chatId);
    }

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