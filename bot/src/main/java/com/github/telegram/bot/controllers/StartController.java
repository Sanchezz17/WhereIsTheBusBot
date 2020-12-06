package com.github.telegram.bot.controllers;

import com.github.telegram.bot.models.Command;
import com.github.telegram.bot.models.Right;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.repos.UserRightRepository;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.MessageType;
import com.github.telegram.mvc.api.TelegramRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;

@BotController
public class StartController {
    private static final Logger log = LogManager.getLogger(StartController.class);

    private final UserRightRepository userRightRepository;

    public StartController(UserRightRepository userRightRepository) {
        this.userRightRepository = userRightRepository;
    }

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
        return sendStartPrompt(user, chatId);
    }

    @BotRequest(value = "/command START_OVER*", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage sendStartPrompt(User user, Long chatId) {
        ArrayList<Command> commandList = new ArrayList<>(Arrays.asList(Command.startCommands));
        if (userRightRepository.existsByUsernameAndRight(user.username(), Right.Admin)) {
            commandList.add(Command.ADMIN_COMMANDS);
        }
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                commandList.toArray(new Command[0]),
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
                transport -> String.format("%s %s", transport.getEmoji(), transport.getName()),
                Transport::getName,
                "transport",
                2);
        return new SendMessage(chatId, "Выберите вид общественного транспорта")
                .replyMarkup(inlineKeyboardMarkup);
    }
}
