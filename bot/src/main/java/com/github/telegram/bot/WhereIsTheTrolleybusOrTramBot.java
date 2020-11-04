package com.github.telegram.bot;

import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.FirstLetter;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.repos.TransportStopRepository;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
@EnableTelegram
@BotController
@Configuration
@EnableJpaRepositories
public class WhereIsTheTrolleybusOrTramBot implements TelegramMvcConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(WhereIsTheTrolleybusOrTramBot.class);
    private HashMap<Integer, Transport> usersTransport = new HashMap<>();

    @Autowired
    private TransportStopRepository transportStopRepository;

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
        ArrayList<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        for (Transport transport : Transport.values())
        {
            String name = transport.getName();
            String callbackData = String.format("/transport %s", name);
            keyboardButtons.add(new InlineKeyboardButton(name).callbackData(callbackData));
        }
        Keyboard inlineKeyboardMarkup = new InlineKeyboardMarkup(
                keyboardButtons.toArray(new InlineKeyboardButton[0]));
        SendMessage response = new SendMessage(chatId, "Выберите вид общественного транспорта");
        response.replyMarkup(inlineKeyboardMarkup);
        return response;
    }

    private SendMessage sendFirstLetterPrompt(Long chatId) {
        //toDo этот метод похож на метод выше, выделить общую логику и переиспользовать
        int chunkSize = 8;
        int lettersCount = FirstLetter.values().length;
        InlineKeyboardButton[][] buttonRows = new InlineKeyboardButton[(int) Math.ceil(lettersCount / (double)chunkSize)][];
        for (int i = 0; i < lettersCount; i++) {
            if (i % chunkSize == 0) {
                buttonRows[i / chunkSize] = new InlineKeyboardButton[Math.min(chunkSize, lettersCount - i)];
            }
            String value = FirstLetter.values()[i].getValue();
            String callbackData = String.format("/letter %s", value);
            InlineKeyboardButton button = new InlineKeyboardButton(value.toString()).callbackData(callbackData);
            buttonRows[i / chunkSize][i % chunkSize] = button;
        }
        Keyboard inlineKeyboardMarkup = new InlineKeyboardMarkup(buttonRows);
        SendMessage response = new SendMessage(chatId, "Выберите первую букву из названия остановки");
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
        FirstLetter letter = FirstLetter.fromStr(letterStr);
        if (letter == null) {
            telegramBot.execute(new SendMessage(chatId, "На эту букву нет остановок"));
            return sendFirstLetterPrompt(chatId);
        }
        if (!usersTransport.containsKey(user.id())) {
            return sendTransportPrompt(chatId);
        }
        TransportStop[] transportStops = transportStopRepository.findByNameStartsWith(letter.getValue())
                .stream().filter(x -> x.transport == usersTransport.get(user.id())).toArray(TransportStop[]::new);
        StringBuilder builder = new StringBuilder();
        for (TransportStop transportStop : transportStops) {
            builder.append(transportStop.name);
            builder.append(" ");
            if (transportStop.direction != null) {
                builder.append(transportStop.direction);
            }
            builder.append("\n");
        }
        return new SendMessage(chatId, builder.toString());
    }
}
