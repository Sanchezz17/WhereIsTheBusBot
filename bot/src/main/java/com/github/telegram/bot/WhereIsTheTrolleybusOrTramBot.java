package com.github.telegram.bot;

import com.github.telegram.bot.models.FirstLetter;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.mvc.api.*;
import com.github.telegram.mvc.config.TelegramBotBuilder;
import com.github.telegram.mvc.config.TelegramMvcConfiguration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;

@SpringBootApplication
@EnableTelegram
@BotController
public class WhereIsTheTrolleybusOrTramBot implements TelegramMvcConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(WhereIsTheTrolleybusOrTramBot.class);
    private HashMap<Integer, Transport> usersTransport = new HashMap<Integer, Transport>();

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(WhereIsTheTrolleybusOrTramBot.class);
    }

    @Override
    public void configuration(TelegramBotBuilder telegramBotBuilder) {
        telegramBotBuilder
                .token(environment.getProperty("telegram.bot.token")).alias("myFirsBean");
    }

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
        SendMessage hello = new SendMessage(chatId, "Привет! Я - бот, который может подсказать" +
                " через сколько минут приедет общественный транспорт на определенную остановку");
        telegramBot.execute(hello);
        return sendTransportPrompt(chatId);
    }

    private SendMessage sendTransportPrompt(Long chatId) {
        ArrayList<InlineKeyboardButton> keyboardButtons = new ArrayList<InlineKeyboardButton>();
        for (Transport transport : Transport.values())
        {
            String name = transport.getName();
            String callbackData = String.format("/transport %s", name);
            keyboardButtons.add(new InlineKeyboardButton(name).callbackData(callbackData));
        }
        Keyboard inlineKeyboardMarkup = new InlineKeyboardMarkup(
                keyboardButtons.toArray(new InlineKeyboardButton[0]));
        SendMessage response = new SendMessage(chatId, "Выбери вид общественного транспорта");
        response.replyMarkup(inlineKeyboardMarkup);
        return response;
    }

    private SendMessage sendFirstLetterPrompt(Long chatId) {
        //toDo этот метод похож на метод выше, выделить общую логику и переиспользовать
        ArrayList<InlineKeyboardButton> keyboardButtons = new ArrayList<InlineKeyboardButton>();
        for (FirstLetter letter : FirstLetter.values())
        {
            Character value = letter.getValue();
            String callbackData = String.format("/letter %s", value);
            keyboardButtons.add(new InlineKeyboardButton(value.toString()).callbackData(callbackData));
        }
        Keyboard inlineKeyboardMarkup = new InlineKeyboardMarkup(
                keyboardButtons.toArray(new InlineKeyboardButton[0]));
        SendMessage response = new SendMessage(chatId, "Введите первую букву из названия остановки");
        response.replyMarkup(inlineKeyboardMarkup);
        return response;
    }

    @BotRequest(value = "/transport *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage setTransportAndSendFirstLetterPrompt(String text, Long chatId, User user, TelegramBot telegramBot) {
        String transportStr = text.split(" ")[1];
        Transport transport = Transport.fromString(transportStr);
        if (transport == null) {
            telegramBot.execute(new SendMessage(chatId, "Неизвестный вид общественного транспорта"));
            return sendTransportPrompt(chatId);
        }
        usersTransport.put(user.id(), transport);
        return sendFirstLetterPrompt(chatId);
    }

    @BotRequest(value = "/letter *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage getTransportStopsByFirstLetter(String text, Long chatId, User user, TelegramBot telegramBot) {
        String letterStr = text.split(" ")[1];
        if (letterStr.length() > 1) {
            telegramBot.execute(new SendMessage(chatId, "Введите одну букву"));
            return sendFirstLetterPrompt(chatId);
        }
        FirstLetter letter = FirstLetter.fromChar(letterStr.charAt(0));
        if (letter == null) {
            telegramBot.execute(new SendMessage(chatId, "На эту букву нет остановок"));
            return sendFirstLetterPrompt(chatId);
        }
        // toDo вернуть список остановок
        return new SendMessage(chatId, "Список остановок");
    }
}
